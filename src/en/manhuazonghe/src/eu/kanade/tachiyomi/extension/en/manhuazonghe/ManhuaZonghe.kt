package eu.kanade.tachiyomi.extension.en.manhuazonghe

import keiyoushix.multisrc.madara.Madara

class ManhuaZonghe : Madara("Manhua Zonghe", "https://manhuazonghe.com", "en") {
    override val useNewChapterEndpoint = false
    override val filterNonMangaItems = false
    override val mangaSubString = "manhua"
}
