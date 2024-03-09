package eu.kanade.tachiyomi.extension.en.freemanhwa

import keiyoushix.multisrc.madara.Madara

class FreeManhwa : Madara("Free Manhwa", "https://manhwas.com", "en") {
    override val useNewChapterEndpoint = false
}
