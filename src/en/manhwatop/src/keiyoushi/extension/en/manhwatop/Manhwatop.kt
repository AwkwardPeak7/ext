package keiyoushi.extension.en.manhwatop

import keiyoushi.multisrc.madara.Madara

class Manhwatop : Madara("Manhwatop", "https://manhwatop.com", "en") {

    // The website does not flag the content.
    override val filterNonMangaItems = false
}
