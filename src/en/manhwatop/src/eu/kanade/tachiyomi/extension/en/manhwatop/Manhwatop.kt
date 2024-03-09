package eu.kanade.tachiyomi.extension.en.manhwatop

import keiyoushix.multisrc.madara.Madara

class Manhwatop : Madara("Manhwatop", "https://manhwatop.com", "en") {

    // The website does not flag the content.
    override val filterNonMangaItems = false
}
