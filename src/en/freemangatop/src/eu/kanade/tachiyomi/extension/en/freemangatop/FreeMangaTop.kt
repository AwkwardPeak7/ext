package eu.kanade.tachiyomi.extension.en.freemangatop

import keiyoushix.multisrc.madara.Madara

class FreeMangaTop : Madara("FreeMangaTop", "https://freemangatop.com", "en") {

    // The website does not flag the content.
    override val filterNonMangaItems = false
}
