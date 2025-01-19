package keiyoushi.extension.ar.paradisebl

import keiyoushi.multisrc.madara.Madara

class ParadiseBL : Madara(
    "Paradise BL",
    "https://paradise-bl.com",
    "ar",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true

    override val mangaDetailsSelectorTitle = ".new-post-title h3 a"
}
