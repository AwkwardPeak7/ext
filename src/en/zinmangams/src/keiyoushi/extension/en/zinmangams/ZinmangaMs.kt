package keiyoushi.extension.en.zinmangams

import keiyoushi.multisrc.madara.Madara

class ZinmangaMs : Madara(
    "Zinmanga.ms",
    "https://zinmanga.ms",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = false

    override val mangaSubString = "manga-1"
}
