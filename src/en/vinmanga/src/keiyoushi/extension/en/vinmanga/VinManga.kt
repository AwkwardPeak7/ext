package keiyoushi.extension.en.vinmanga

import keiyoushi.multisrc.madara.Madara

class VinManga : Madara("VinManga", "https://vinload.com", "en") {
    override val useNewChapterEndpoint = true
}
