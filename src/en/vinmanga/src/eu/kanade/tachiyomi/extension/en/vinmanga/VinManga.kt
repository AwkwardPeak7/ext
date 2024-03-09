package eu.kanade.tachiyomi.extension.en.vinmanga

import keiyoushix.multisrc.madara.Madara

class VinManga : Madara("VinManga", "https://vinload.com", "en") {
    override val useNewChapterEndpoint = true
}
