package keiyoushi.extension.es.uchuujinprojects

import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.SimpleDateFormat
import java.util.Locale

class UchuujinProjects : MangaThemesia(
    "Uchuujin Projects",
    "https://uchuujinmangas.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val client = super.client.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 3, 1)
        .build()

    override val hasProjectPage = true
}
