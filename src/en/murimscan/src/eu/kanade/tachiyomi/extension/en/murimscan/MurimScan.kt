package eu.kanade.tachiyomi.extension.en.murimscan

import keiyoushix.multisrc.madara.Madara

class MurimScan : Madara("MurimScan", "https://murimscan.run", "en") {
    override val useNewChapterEndpoint = false
}
