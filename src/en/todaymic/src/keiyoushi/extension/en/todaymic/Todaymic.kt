package keiyoushi.extension.en.todaymic

import keiyoushi.multisrc.madara.Madara

class Todaymic : Madara("Todaymic", "https://todaymic.com", "en") {
    override val filterNonMangaItems = false
    override val useNewChapterEndpoint = true
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val mangaDetailsSelectorDescription = ".manga-about > p:nth-child(2)"
}
