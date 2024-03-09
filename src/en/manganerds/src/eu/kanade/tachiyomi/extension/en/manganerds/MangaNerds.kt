package eu.kanade.tachiyomi.extension.en.manganerds

import keiyoushix.multisrc.madara.Madara

class MangaNerds : Madara("Manga Nerds", "https://manganerds.com", "en") {
    override val useNewChapterEndpoint = true
}
