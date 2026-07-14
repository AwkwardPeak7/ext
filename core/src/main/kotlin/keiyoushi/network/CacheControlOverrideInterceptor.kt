package keiyoushi.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites the origin's `Cache-Control` response header so OkHttp's private disk cache
 * treats the response as fresh for as long as the *request* itself already asked for via
 * its own `Cache-Control: max-age`, instead of revalidating on every access.
 *
 * Some servers only send `s-maxage` (a shared/CDN-cache directive OkHttp ignores) or a
 * `Last-Modified` stamped identical to `Date` (which makes OkHttp's heuristic freshness
 * calculation - roughly 10% of `Date - Last-Modified` - come out to ~0). Either case makes
 * OkHttp consider the cached response stale immediately, so it sends a conditional
 * `If-Modified-Since`/`If-None-Match` request (getting back a cheap `304`) on every access
 * instead of serving straight from cache, even though the caller (e.g. [OkHttpClient.get])
 * already declared it's fine with a response up to `max-age` old.
 *
 * Must be added as a **network** interceptor (`addNetworkInterceptor`, not `addInterceptor`),
 * since OkHttp's cache-storage decision happens in `CacheInterceptor`, which sits above the
 * network interceptors: only a network interceptor's changes are visible to it before the
 * response gets cached.
 *
 * Only fills in the gap when the request specified a `max-age` and the origin has otherwise
 * signalled the response is cacheable (`public`/`s-maxage`) but omitted a private-cache-usable
 * directive; it leaves `no-store`, `no-cache`, and `private` responses untouched, and never
 * widens an existing `max-age`.
 */
class CacheControlOverrideInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val requestMaxAge = request.cacheControl.maxAgeSeconds
        val cacheControl = response.header("Cache-Control")
        if (requestMaxAge <= 0 || !response.isSuccessful || cacheControl == null || !shouldOverride(cacheControl)) {
            return response
        }

        return response.newBuilder()
            .header("Cache-Control", "public, max-age=$requestMaxAge")
            .build()
    }

    private fun shouldOverride(cacheControl: String): Boolean {
        val directives = cacheControl.split(",").map { it.trim().lowercase() }
        if (directives.any { it == "no-store" || it == "no-cache" || it == "private" }) return false
        if (directives.any { it.startsWith("max-age=") }) return false
        return true
    }
}
