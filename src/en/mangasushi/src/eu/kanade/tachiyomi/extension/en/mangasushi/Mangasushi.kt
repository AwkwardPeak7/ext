package eu.kanade.tachiyomi.extension.en.mangasushi

import keiyoushix.multisrc.madara.Madara

class Mangasushi : Madara("Mangasushi", "https://mangasushi.org", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
