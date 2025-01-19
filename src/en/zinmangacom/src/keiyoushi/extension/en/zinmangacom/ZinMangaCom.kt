package keiyoushi.extension.en.zinmangacom

import keiyoushi.multisrc.madara.Madara

class ZinMangaCom : Madara(
    "Zin-Manga.com",
    "https://zin-manga.com",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true

    override val filterNonMangaItems = false
}
