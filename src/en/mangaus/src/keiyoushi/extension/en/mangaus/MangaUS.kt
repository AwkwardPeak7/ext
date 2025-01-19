package keiyoushi.extension.en.mangaus

import keiyoushi.multisrc.madara.Madara

class MangaUS : Madara("MangaUS", "https://mangaus.xyz", "en") {
    override val pageListParseSelector = "img"
}
