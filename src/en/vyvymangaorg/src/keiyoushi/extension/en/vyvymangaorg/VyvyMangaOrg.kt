package keiyoushi.extension.en.vyvymangaorg

import keiyoushi.multisrc.madara.Madara

class VyvyMangaOrg : Madara(
    name = "VyvyManga.org",
    baseUrl = "https://vyvymanga.org",
    lang = "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
