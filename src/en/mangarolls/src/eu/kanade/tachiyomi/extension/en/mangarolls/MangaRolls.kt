package eu.kanade.tachiyomi.extension.en.mangarolls

import keiyoushix.multisrc.madara.Madara

class MangaRolls : Madara("MangaRolls", "https://mangarolls.net", "en") {
    override val useNewChapterEndpoint = true
}
