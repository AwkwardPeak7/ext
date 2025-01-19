package keiyoushi.extension.all.mangacrazy

import keiyoushi.multisrc.madara.Madara

class MangaCrazy : Madara("MangaCrazy", "https://mangacrazy.net", "all") {
    override val useNewChapterEndpoint = true
}
