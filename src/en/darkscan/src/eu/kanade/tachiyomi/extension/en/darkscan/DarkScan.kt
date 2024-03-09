package eu.kanade.tachiyomi.extension.en.darkscan

import keiyoushix.multisrc.madara.Madara

class DarkScan : Madara("Dark-scan", "https://dark-scan.com", "en") {
    override val useNewChapterEndpoint = true
}
