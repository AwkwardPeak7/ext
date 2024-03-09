package eu.kanade.tachiyomi.extension.en.whalemanga

import keiyoushix.multisrc.madara.Madara

class WhaleManga : Madara("WhaleManga", "https://whalemanga.com", "en") {
    override val useNewChapterEndpoint = true
}
