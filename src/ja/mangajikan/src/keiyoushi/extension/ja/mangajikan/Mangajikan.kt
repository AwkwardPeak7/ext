package keiyoushi.extension.ja.mangajikan

import keiyoushi.multisrc.mccms.MCCMS
import keiyoushi.multisrc.mccms.MCCMSConfig

class Mangajikan : MCCMS(
    name = "漫画時間",
    baseUrl = "https://www.mangajikan.com",
    lang = "ja",
    config = MCCMSConfig(lazyLoadImageAttr = "src"),
)
