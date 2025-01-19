package keiyoushi.extension.en.luxmanga

import keiyoushi.multisrc.madara.Madara

class LuxManga : Madara("LuxManga", "https://luxmanga.net", "en") {
    override val useNewChapterEndpoint = false
}
