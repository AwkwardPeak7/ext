package keiyoushi.extension.ca.fansubscathentai

import keiyoushi.multisrc.fansubscat.FansubsCat

class HentaiCat : FansubsCat(
    "Hentai.cat",
    "https://manga.hentai.cat",
    "ca",
    "https://api.hentai.cat",
    isHentaiSite = true,
) {
    override val id: Long = 7575385310756416449
}
