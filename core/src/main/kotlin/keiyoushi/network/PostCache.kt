package keiyoushi.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.Buffer

private const val CACHE_KEY_PARAM = "kei-post-cache-key"

/**
 * Lets OkHttp's own [okhttp3.Cache] (normally GET-only) cache POST requests, for things like
 * search endpoints that are POST only because of a request body, not because they have side
 * effects.
 *
 * OkHttp's cache-eligibility check is hardcoded to reject any non-GET request, with no public
 * hook to change that. This pair of interceptors works around it by masking an eligible POST as
 * a GET purely for [okhttp3.internal.cache.CacheInterceptor]'s benefit, then restoring the real
 * POST right before it would hit the wire:
 *
 * - [PostCacheRewriteInterceptor] is an **application** interceptor (`addInterceptor`), so it
 *   runs before CacheInterceptor. It rewrites the request into a GET whose URL embeds a hash of
 *   the request body as a cache key, and tags it with the original request.
 * - CacheInterceptor now sees an ordinary GET. On a hit, it returns straight from disk and
 *   nothing below it - including network interceptors - ever runs.
 * - On a miss, [PostCacheRestoreInterceptor] is a **network** interceptor (`addNetworkInterceptor`),
 *   running just before the request hits the wire. It swaps the masked GET back out for the
 *   original POST + body, which is what actually gets sent.
 *
 * A request only gets masked if it's a POST with an explicit `Cache-Control: max-age` set (e.g.
 * via [OkHttpClient.post]'s `cacheControl` parameter) - this is opt-in per request, not a blanket
 * "cache all POSTs".
 *
 * Add both interceptors to the client for this to work; one without the other does nothing.
 */
class PostCacheRewriteInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.method != "POST" || original.cacheControl.maxAgeSeconds <= 0) {
            return chain.proceed(original)
        }

        val bodyHash = original.body?.let { hashRequestBody(it) } ?: "empty"

        val maskedUrl = original.url.newBuilder()
            .addQueryParameter(CACHE_KEY_PARAM, bodyHash)
            .build()

        val masked = original.newBuilder()
            .url(maskedUrl)
            .get()
            .tag(Request::class.java, original)
            .build()

        return chain.proceed(masked)
    }

    private fun hashRequestBody(body: RequestBody): String {
        val buffer = Buffer()
        body.writeTo(buffer)
        return buffer.readByteString().sha256().hex()
    }
}

class PostCacheRestoreInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val masked = chain.request()
        val original = masked.tag(Request::class.java) ?: return chain.proceed(masked)

        // Start from the masked request, not the stashed original: BridgeInterceptor already ran
        // on it (Host header for HTTP/2's :authority, Cookie from the CookieJar, etc.), while the
        // stashed original never went through BridgeInterceptor at all.
        val body = original.body
        val restored = masked.newBuilder()
            .url(original.url)
            .method(original.method, body)
            .apply {
                // BridgeInterceptor derives Content-Type/Content-Length from the request body,
                // but it only ever saw the masked GET, which has none - so these never got added
                // for the restored POST either. Without an explicit Content-Type, a server can't
                // tell it's form-encoded and won't populate $_POST/equivalent from the body.
                if (body != null) {
                    body.contentType()?.let { header("Content-Type", it.toString()) }
                    val contentLength = body.contentLength()
                    if (contentLength != -1L) {
                        header("Content-Length", contentLength.toString())
                        removeHeader("Transfer-Encoding")
                    } else {
                        header("Transfer-Encoding", "chunked")
                        removeHeader("Content-Length")
                    }
                }
            }
            .build()

        return chain.proceed(restored)
    }
}
