package keiyoushi.extension.en.manga1k

import keiyoushi.multisrc.madara.Madara

class Manga1k : Madara(
    "Manga1k",
    "https://manga1k.com",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false
}
