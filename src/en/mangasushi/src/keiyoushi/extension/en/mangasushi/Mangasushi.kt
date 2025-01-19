package keiyoushi.extension.en.mangasushi

import keiyoushi.multisrc.madara.Madara

class Mangasushi : Madara("Mangasushi", "https://mangasushi.org", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
