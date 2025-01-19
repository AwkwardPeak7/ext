package keiyoushi.extension.ja.rawmanga

import keiyoushi.multisrc.madara.Madara

class MangaRAW : Madara(
    "MangaRAW",
    "https://rawmanga.su",
    "ja",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true

    override val filterNonMangaItems = false

    override val mangaSubString = "r"
}
