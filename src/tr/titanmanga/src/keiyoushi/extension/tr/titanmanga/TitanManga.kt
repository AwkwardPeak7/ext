package keiyoushi.extension.tr.titanmanga

import keiyoushi.multisrc.madara.Madara

class TitanManga : Madara(
    "Titan Manga",
    "https://titanmanga.com",
    "tr",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true
}
