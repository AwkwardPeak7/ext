package keiyoushi.extension.en.mangarolls

import keiyoushi.multisrc.madara.Madara

class MangaRolls : Madara("MangaRolls", "https://mangarolls.net", "en") {
    override val useNewChapterEndpoint = true
}
