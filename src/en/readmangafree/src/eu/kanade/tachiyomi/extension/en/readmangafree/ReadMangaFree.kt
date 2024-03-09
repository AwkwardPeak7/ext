package eu.kanade.tachiyomi.extension.en.readmangafree

import keiyoushix.multisrc.madara.Madara

class ReadMangaFree : Madara("ReadMangaFree", "https://readmangafree.net", "en") {
    override val useNewChapterEndpoint = false
}
