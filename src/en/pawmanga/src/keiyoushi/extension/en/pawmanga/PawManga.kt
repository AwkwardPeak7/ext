package keiyoushi.extension.en.pawmanga

import keiyoushi.multisrc.madara.Madara

class PawManga : Madara("Paw Manga", "https://pawmanga.com", "en") {
    override val useNewChapterEndpoint = true
}
