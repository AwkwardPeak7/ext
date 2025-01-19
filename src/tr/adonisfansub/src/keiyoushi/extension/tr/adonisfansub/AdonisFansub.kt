package keiyoushi.extension.tr.adonisfansub

import eu.kanade.tachiyomi.network.GET
import keiyoushi.multisrc.madara.Madara
import okhttp3.Request

class AdonisFansub : Madara("Adonis Fansub", "https://manga.adonisfansub.com", "tr") {
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/manga/page/$page/?m_orderby=views", headers)
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/manga/page/$page/?m_orderby=latest", headers)
}
