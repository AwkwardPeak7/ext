package keiyoushi.extension.en.manhwafreakxyz

import keiyoushi.multisrc.mangathemesia.MangaThemesia

class ManhwaFreakXyz : MangaThemesia(
    "ManhwaFreak.xyz",
    "https://manhwafreak.xyz",
    "en",
) {
    override val seriesStatusSelector = ".status-value"
}
