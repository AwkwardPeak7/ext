package eu.kanade.tachiyomi.extension.en.mangacultivator

import keiyoushix.multisrc.madara.Madara

class MangaCultivator : Madara("MangaCultivator", "https://mangacultivator.com", "en") {
    override val useNewChapterEndpoint: Boolean = true
}
