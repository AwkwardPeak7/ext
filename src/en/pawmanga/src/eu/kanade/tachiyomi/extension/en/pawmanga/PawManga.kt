package eu.kanade.tachiyomi.extension.en.pawmanga

import keiyoushix.multisrc.madara.Madara

class PawManga : Madara("Paw Manga", "https://pawmanga.com", "en") {
    override val useNewChapterEndpoint = true
}
