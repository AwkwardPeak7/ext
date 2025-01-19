package keiyoushi.extension.fr.etheralradiance

import keiyoushi.lib.cookieinterceptor.CookieInterceptor
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.SimpleDateFormat
import java.util.Locale

class EtheralRadiance : MangaThemesia(
    "Etheral Radiance",
    "https://www.etheralradiance.eu",
    "fr",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("fr")),
) {
    // Cookie required for pages
    private val cookieInterceptor = CookieInterceptor(baseUrl.toHttpUrl().host, "_lscache_vary" to "1")

    override val client = network.cloudflareClient.newBuilder()
        .addNetworkInterceptor(cookieInterceptor)
        .build()
}
