package keiyoushi.extension.en.manhwatoon

import keiyoushi.multisrc.madara.Madara

class ManhwaToon : Madara(
    "Manhwa Toon",
    "https://www.manhwatoon.com",
    "en",
) {
    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
