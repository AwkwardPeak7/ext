package eu.kanade.tachiyomi.extension.en.mangarosie

import keiyoushix.multisrc.madara.Madara

class MangaRosie : Madara("MangaRosie", "https://mangarosie.in", "en") {
    override val useNewChapterEndpoint = false
    override val mangaDetailsSelectorStatus = "div.summary-heading:contains(Status) + div.summary-content"
}
