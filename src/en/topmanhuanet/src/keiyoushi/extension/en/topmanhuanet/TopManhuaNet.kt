package keiyoushi.extension.en.topmanhuanet

import keiyoushi.multisrc.madara.Madara

class TopManhuaNet : Madara(
    "TopManhua.net",
    "https://topmanhua.net",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true
}
