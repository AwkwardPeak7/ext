package keiyoushi.extension.es.inarimanga

import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.SimpleDateFormat
import java.util.Locale

class VisorInari : MangaThemesia(
    "Visor Inari",
    "https://inarimanga.visahelper.online",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val id = 7949577653918285764

    override val client = super.client.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 3, 1)
        .build()
}
