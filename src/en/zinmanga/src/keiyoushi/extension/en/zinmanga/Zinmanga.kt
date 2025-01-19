package keiyoushi.extension.en.zinmanga

import keiyoushi.multisrc.madara.Madara

class Zinmanga : Madara("Zinmanga", "https://mangazin.org", "en") {

    // The website does not flag the content consistently.
    override val filterNonMangaItems = false
}
