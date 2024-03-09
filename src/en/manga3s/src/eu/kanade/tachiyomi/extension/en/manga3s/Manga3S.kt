package eu.kanade.tachiyomi.extension.en.manga3s

import keiyoushix.multisrc.madara.Madara

class Manga3S : Madara("Manga3S", "https://manga3s.com", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
