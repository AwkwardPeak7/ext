package keiyoushi.lib.webcrack

import keiyoushi.utils.parseAs
import keiyoushi.utils.runWebView
import keiyoushi.utils.toJsonString
import kotlinx.serialization.Serializable

/**
 * Deobfuscates JavaScript with [webcrack](https://github.com/j4k0xb/webcrack), which handles
 * obfuscator.io output (string arrays, control flow flattening, dead code, self defending, etc.)
 * and unminifies the result.
 *
 * Runs in a WebView, since webcrack is async and too stack-hungry for QuickJS.
 * The bundled script is built from `lib/webcrack/js`; see the README there.
 */
object Webcrack {

    suspend fun deobfuscate(source: String): String = runWebView {
        webMessageBridge(BRIDGE) { message ->
            val result = message.parseAs<Result>()
            if (result.code != null) {
                resolve(result.code)
            } else {
                reject(Exception("webcrack failed: ${result.error}"))
            }
        }
        onPageFinished {
            evaluateJs(javaClass.getResource("/assets/webcrack.js")!!.readText())
            evaluateJs(
                """
                webcrack.deobfuscate(${source.toJsonString()}).then(
                    code => $BRIDGE.postMessage(JSON.stringify({ code })),
                    e => $BRIDGE.postMessage(JSON.stringify({ error: String(e) })),
                )
                """.trimIndent(),
            )
        }
        loadData("about:blank", "")
    }
}

private const val BRIDGE = "webcrackBridge"

@Serializable
private class Result(
    val code: String? = null,
    val error: String? = null,
)
