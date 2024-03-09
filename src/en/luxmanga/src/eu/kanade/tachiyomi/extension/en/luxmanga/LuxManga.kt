package eu.kanade.tachiyomi.extension.en.luxmanga

import keiyoushix.multisrc.madara.Madara

class LuxManga : Madara("LuxManga", "https://luxmanga.net", "en") {
    override val useNewChapterEndpoint = false
}
