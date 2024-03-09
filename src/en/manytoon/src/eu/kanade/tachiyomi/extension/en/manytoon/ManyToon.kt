package eu.kanade.tachiyomi.extension.en.manytoon

import keiyoushix.multisrc.madara.Madara

class ManyToon : Madara("ManyToon", "https://manytoon.com", "en") {

    override val mangaSubString = "comic"

    override val useNewChapterEndpoint: Boolean = true
}
