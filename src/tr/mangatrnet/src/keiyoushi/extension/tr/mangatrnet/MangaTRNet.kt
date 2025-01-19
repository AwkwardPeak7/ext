package keiyoushi.extension.tr.mangatrnet

import keiyoushi.multisrc.madara.Madara

class MangaTRNet : Madara(
    "MangaTR.net",
    "https://mangatr.net",
    "tr",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true
}
