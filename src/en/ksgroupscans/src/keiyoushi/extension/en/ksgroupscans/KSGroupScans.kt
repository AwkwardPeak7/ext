package keiyoushi.extension.en.ksgroupscans

import keiyoushi.multisrc.madara.Madara

class KSGroupScans : Madara("KSGroupScans", "https://ksgroupscans.com", "en") {
    override val versionId = 2
    override val useNewChapterEndpoint = true
}
