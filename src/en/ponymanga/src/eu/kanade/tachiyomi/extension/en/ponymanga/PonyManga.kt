package eu.kanade.tachiyomi.extension.en.ponymanga

import keiyoushix.multisrc.madara.Madara

class PonyManga : Madara("Pony Manga", "https://ponymanga.com", "en") {
    override val useNewChapterEndpoint = false
}
