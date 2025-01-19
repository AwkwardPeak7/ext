package keiyoushi.extension.id.komiksan

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import org.jsoup.nodes.Document

class Komiksan : MangaThemesia("Komiksan", "https://komiksan.link", "id", "/list") {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override fun mangaDetailsParse(document: Document) = super.mangaDetailsParse(document).apply {
        title = document.selectFirst(seriesThumbnailSelector)!!.attr("alt")
    }
}
