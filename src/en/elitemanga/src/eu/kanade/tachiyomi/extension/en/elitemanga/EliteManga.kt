package eu.kanade.tachiyomi.extension.en.elitemanga

import keiyoushix.multisrc.madara.Madara

class EliteManga : Madara("Elite Manga", "https://www.elitemanga.org", "en") {
    override val useNewChapterEndpoint = true
    override val filterNonMangaItems = false
}
