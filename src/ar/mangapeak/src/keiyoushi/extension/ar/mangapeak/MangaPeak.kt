package keiyoushi.extension.ar.mangapeak

import keiyoushi.multisrc.madara.Madara

class MangaPeak : Madara(
    "MangaPeak",
    "https://mangapeak.org",
    "ar",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true
}
