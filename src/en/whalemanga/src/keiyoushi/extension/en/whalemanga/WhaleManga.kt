package keiyoushi.extension.en.whalemanga

import keiyoushi.multisrc.madara.Madara

class WhaleManga : Madara("WhaleManga", "https://whalemanga.com", "en") {
    override val useNewChapterEndpoint = true
}
