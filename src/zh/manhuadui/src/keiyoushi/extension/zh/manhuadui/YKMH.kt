package keiyoushi.extension.zh.manhuadui

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushi.multisrc.sinmh.SinMH
import org.jsoup.nodes.Document

// This site blocks IP outside China
class YKMH : SinMH("优酷漫画", "https://www.ykmh.net") {
    override val id = 1637952806167036168

    override fun mangaDetailsParse(document: Document) = mangaDetailsParseDMZJStyle(document, hasBreadcrumb = false)

    override fun List<SChapter>.sortedDescending() = this
}
