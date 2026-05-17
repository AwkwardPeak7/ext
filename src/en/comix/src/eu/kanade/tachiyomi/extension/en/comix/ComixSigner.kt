package eu.kanade.tachiyomi.extension.en.comix

import app.cash.quickjs.QuickJs
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.awaitSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.jsoup.Jsoup

/**
 * Generates the `_=<signature>` query parameter that comix.to requires on some
 * GET requests. The signature is produced by an obfuscated client module
 * (`secure.js`) that the site loads and installs as an axios request
 * interceptor. Rather than reimplementing the cipher, this downloads the real
 * `secure.js` and runs it inside QuickJS with a small browser shim.
 */
class ComixSigner(
    private val client: OkHttpClient,
    private val headers: Headers,
    private val baseUrl: String,
) {

    // Built once, lazily. Every sign() call executes the bytecode in a fresh
    // QuickJs instance — keeps signing thread-safe without a long-lived handle.
    private val bytecodeMutex = Mutex()
    private var cachedBytecode: ByteArray? = null

    suspend fun sign(path: String): String? = QuickJs.create().use { qjs ->
        qjs.execute(bytecode())
        qjs.evaluate("comixSign(${jsStringLiteral(path)})") as String?
    }

    private suspend fun bytecode(): ByteArray = cachedBytecode ?: bytecodeMutex.withLock {
        cachedBytecode ?: compileBundle().also { cachedBytecode = it }
    }

    private suspend fun compileBundle(): ByteArray {
        val (secureJs, cfg) = fetchSecureJsAndCfg()
        val bundle = buildString {
            append("var __COMIX_CFG__ = ").append(jsStringLiteral(cfg)).append(";\n")
            append(loadAsset("prelude.js")).append('\n')
            append(stripEsmExport(secureJs)).append('\n')
            append(loadAsset("epilogue.js"))
        }
        return QuickJs.create().use { it.compile(bundle, "comix-signer-bundle.js") }
    }

    private suspend fun fetchSecureJsAndCfg(): Pair<String, String> {
        val home = baseUrl.toHttpUrl()
        val document = Jsoup.parse(fetch(home), home.toString())
        val cfg = document.selectFirst("meta[name=cfg]")?.attr("content")
            ?: throw Exception("ComixSigner: missing <meta name=\"cfg\"> in homepage")
        val mainUrl = document.selectFirst("script[src*=/main-][src$=.js]")
            ?.absUrl("src")?.toHttpUrlOrNull()
            ?: throw Exception("ComixSigner: missing main-*.js reference in homepage")

        val mainJs = fetch(mainUrl)
        val secureName = SECURE_JS_RE.find(mainJs)?.value
            ?: throw Exception("ComixSigner: missing secure-*.js reference in main.js")
        val secureUrl = mainUrl.resolve(secureName)
            ?: throw Exception("ComixSigner: invalid secure-*.js URL: $secureName")

        return fetch(secureUrl) to cfg
    }

    private suspend fun fetch(url: HttpUrl): String = client.newCall(GET(url, headers)).awaitSuccess().use { it.body.string() }

    // secure.js is an ES module whose only module syntax is a single trailing
    //     export { g as i, v as n, Vr as r, b as t };
    // QuickJS evaluates plain scripts, not modules — rewrite that to:
    //     globalThis.__SECURE_EXPORTS = { i: g, n: v, r: Vr, t: b };
    private fun stripEsmExport(secureJs: String): String {
        val src = secureJs.trim()
        val match = EXPORT_RE.find(src)
            ?: throw Exception("ComixSigner: missing trailing ESM export in secure.js")

        val pairs = match.groupValues[1]
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(",") { piece ->
                val parts = piece.split(" as ")
                if (parts.size == 2) "${parts[1].trim()}:${parts[0].trim()}" else "$piece:$piece"
            }
        return src.substring(0, match.range.first) + "globalThis.__SECURE_EXPORTS={$pairs};"
    }

    private fun loadAsset(name: String): String = javaClass.getResourceAsStream("/assets/$name")
        ?.bufferedReader()?.use { it.readText() }
        ?: throw Exception("ComixSigner: missing bundled asset $name")

    // JS string literal encoder — handles ", \, control chars, and U+2028/9.
    private fun jsStringLiteral(s: String): String = buildString(s.length + 2) {
        append('"')
        for (c in s) {
            when {
                c == '\\' -> append("\\\\")
                c == '"' -> append("\\\"")
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c.code == 0x2028 -> append("\\u2028")
                c.code == 0x2029 -> append("\\u2029")
                c.code < 0x20 -> append("\\u").append(c.code.toString(16).padStart(4, '0'))
                else -> append(c)
            }
        }
        append('"')
    }

    companion object {
        private val SECURE_JS_RE = Regex("""secure-[A-Za-z0-9_-]+\.js""")
        private val EXPORT_RE = Regex("""export\s*\{([^}]*)\}\s*;?\s*$""")
    }
}
