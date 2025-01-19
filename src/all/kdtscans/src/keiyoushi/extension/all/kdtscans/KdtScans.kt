package keiyoushi.extension.all.kdtscans

import eu.kanade.tachiyomi.source.model.SManga
import keiyoushi.multisrc.madara.Madara
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.Locale

class KdtScans : Madara(
    "KDT Scans",
    "https://kdtscans.com",
    "all",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
    override val fetchGenres = false

    override fun popularMangaFromElement(element: Element): SManga {
        return super.popularMangaFromElement(element).apply {
            title = title.cleanupTitle()
        }
    }

    override fun searchMangaFromElement(element: Element): SManga {
        return super.searchMangaFromElement(element).apply {
            title = title.cleanupTitle()
        }
    }

    override fun mangaDetailsParse(document: Document): SManga {
        return super.mangaDetailsParse(document).apply {
            title = title.cleanupTitle()
        }
    }

    private fun String.cleanupTitle() = replace(titleCleanupRegex, "").trim()

    private val titleCleanupRegex =
        Regex("""^\[(ESPAÑOL|English)\]\s+(–\s+)?""", RegexOption.IGNORE_CASE)
}
