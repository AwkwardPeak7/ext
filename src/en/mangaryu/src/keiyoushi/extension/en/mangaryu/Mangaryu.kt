package keiyoushi.extension.en.mangaryu

import keiyoushi.multisrc.madara.Madara

class Mangaryu : Madara("Mangaryu", "https://mangaryu.com", "en") {
    override val useNewChapterEndpoint = false
}
