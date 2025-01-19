package keiyoushi.extension.id.izanamiscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale

class IzanamiScans : MangaThemesia(
    "Izanami Scans",
    "https://izanamiscans.my.id",
    "id",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val seriesAuthorSelector = ".fmed b:contains(Penulis) + span"
}
