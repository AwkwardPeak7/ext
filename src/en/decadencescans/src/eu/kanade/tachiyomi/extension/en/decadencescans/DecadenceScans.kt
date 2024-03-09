package eu.kanade.tachiyomi.extension.en.decadencescans

import keiyoushix.multisrc.madara.Madara

class DecadenceScans : Madara("Decadence Scans", "https://reader.decadencescans.com", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
