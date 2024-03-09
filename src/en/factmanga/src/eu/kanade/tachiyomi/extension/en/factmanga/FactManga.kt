package eu.kanade.tachiyomi.extension.en.factmanga

import keiyoushix.multisrc.madara.Madara

class FactManga : Madara("FactManga", "https://factmanga.com", "en") {
    override val useNewChapterEndpoint = true
}
