package eu.kanade.tachiyomi.extension.en.mangakitsu

import keiyoushix.multisrc.madara.Madara

class MangaKitsu : Madara("Manga Kitsu", "https://mangakitsu.com", "en") {
    override val useNewChapterEndpoint = false
    override val mangaDetailsSelectorStatus = "div.summary-heading:contains(Status) + div.summary-content"
}
