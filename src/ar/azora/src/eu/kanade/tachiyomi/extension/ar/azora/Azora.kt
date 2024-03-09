package eu.kanade.tachiyomi.extension.ar.azora

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushix.multisrc.madara.Madara
import org.jsoup.nodes.Element

class Azora : Madara("Azora", "https://azoramoon.com", "ar") {
    override val mangaSubString = "series"
    override val useNewChapterEndpoint = false
    override fun chapterListSelector() = "li.wp-manga-chapter:not(.premium-block)" // Filter fake chapters
    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()

        element.select("a").let {
            chapter.url = it.attr("href").substringAfter(baseUrl)
            chapter.name = it.text()
        }
        return chapter
    }
}
