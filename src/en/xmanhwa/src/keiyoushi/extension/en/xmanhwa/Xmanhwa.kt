package keiyoushi.extension.en.xmanhwa

import keiyoushi.multisrc.madara.Madara

class Xmanhwa : Madara(
    "Xmanhwa",
    "https://www.xmanhwa.me",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true

    override val filterNonMangaItems = false
}
