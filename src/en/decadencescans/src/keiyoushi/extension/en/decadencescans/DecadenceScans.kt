package keiyoushi.extension.en.decadencescans

import keiyoushi.multisrc.madara.Madara

class DecadenceScans : Madara("Decadence Scans", "https://reader.decadencescans.com", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
