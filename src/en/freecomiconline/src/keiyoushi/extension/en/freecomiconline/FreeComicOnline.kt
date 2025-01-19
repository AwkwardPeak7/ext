package keiyoushi.extension.en.freecomiconline

import keiyoushi.multisrc.madara.Madara

class FreeComicOnline : Madara(
    "Free Comic Online",
    "https://freecomiconline.me",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false

    override val mangaSubString = "comic"
}
