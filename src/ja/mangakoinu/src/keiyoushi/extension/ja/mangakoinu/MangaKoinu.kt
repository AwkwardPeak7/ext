package keiyoushi.extension.ja.mangakoinu

import keiyoushi.multisrc.mccms.MCCMS
import keiyoushi.multisrc.mccms.MCCMSConfig

class MangaKoinu : MCCMS(
    name = "Manga Koinu",
    baseUrl = "https://www.mangakoinu.com",
    lang = "ja",
    config = MCCMSConfig(lazyLoadImageAttr = "src"),
)
