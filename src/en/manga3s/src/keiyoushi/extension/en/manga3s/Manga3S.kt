package keiyoushi.extension.en.manga3s

import keiyoushi.multisrc.madara.Madara

class Manga3S : Madara("Manga3S", "https://manga3s.com", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
