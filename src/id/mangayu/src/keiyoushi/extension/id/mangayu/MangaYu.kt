package keiyoushi.extension.id.mangayu

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class MangaYu : MangaThemesia(
    "MangaYu",
    "https://mangayu.id",
    "id",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(20, 5)
        .build()
}
