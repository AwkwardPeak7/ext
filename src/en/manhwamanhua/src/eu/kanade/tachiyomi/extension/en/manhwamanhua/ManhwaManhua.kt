package eu.kanade.tachiyomi.extension.en.manhwamanhua

import keiyoushix.multisrc.madara.Madara

class ManhwaManhua : Madara("ManhwaManhua", "https://manhwamanhua.com", "en") {
    override val useNewChapterEndpoint = true
    override val filterNonMangaItems = false
}
