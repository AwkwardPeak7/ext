package keiyoushi.extension.en.darkscancom

import keiyoushi.multisrc.madara.Madara

class DarkScanCom : Madara(
    "Dark-Scan.com",
    "https://dark-scan.com",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true

    override val mangaDetailsSelectorStatus = "div.summary-heading:contains(Status) + div.summary-content"
}
