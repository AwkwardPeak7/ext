package keiyoushi.extension.en.kingofscans

import eu.kanade.tachiyomi.source.model.Page
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import org.jsoup.nodes.Document

class KingOfScans : MangaThemesia(
    "King of Scans",
    "https://kingofscans.com",
    "en",
    mangaUrlDirectory = "/comics",
) {
    // Strip out position- and name-dependant ads. "Read first at ..."
    override fun pageListParse(document: Document): List<Page> {
        val pages = super.pageListParse(document)
        return pages.filterIndexed { index, page ->
            when (index) {
                0 -> !page.imageUrl!!.endsWith("/START-KS.jpg")
                pages.lastIndex -> !page.imageUrl!!.endsWith("/END-KS.jpg")
                else -> true
            }
        }
    }
}
