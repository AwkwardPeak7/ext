package keiyoushi.extension.en.murimscan

import keiyoushi.multisrc.madara.Madara

class MurimScan : Madara("MurimScan", "https://murimscan.run", "en") {
    override val useNewChapterEndpoint = false
}
