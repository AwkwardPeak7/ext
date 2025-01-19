package keiyoushi.extension.en.factmanga

import keiyoushi.multisrc.madara.Madara

class FactManga : Madara("FactManga", "https://factmanga.com", "en") {
    override val useNewChapterEndpoint = true
}
