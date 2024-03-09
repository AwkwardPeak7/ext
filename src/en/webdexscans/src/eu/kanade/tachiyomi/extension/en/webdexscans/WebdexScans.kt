package eu.kanade.tachiyomi.extension.en.webdexscans

import keiyoushix.multisrc.madara.Madara

class WebdexScans : Madara("Webdex Scans", "https://webdexscans.com", "en") {
    override val useNewChapterEndpoint = true
    override val mangaDetailsSelectorStatus = "div.summary-heading:contains(Status) + div.summary-content"
}
