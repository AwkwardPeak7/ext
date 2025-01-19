package keiyoushi.extension.es.celestialmoon

import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import keiyoushi.lib.cookieinterceptor.CookieInterceptor
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class CelestialMoon : MangaThemesia(
    "Celestial Moon",
    "https://celestialmoonscan.es",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    // ZeistManga -> MangaThemesia
    override val versionId = 2

    private val cookieInterceptor = CookieInterceptor(baseUrl.substringAfter("://"), "age_gate" to "18")

    override val client = super.client.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 3, 1, TimeUnit.SECONDS)
        .addNetworkInterceptor(cookieInterceptor)
        .build()
}
