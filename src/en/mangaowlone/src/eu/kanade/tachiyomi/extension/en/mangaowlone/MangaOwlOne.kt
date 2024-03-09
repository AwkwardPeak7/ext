package eu.kanade.tachiyomi.extension.en.mangaowlone

import keiyoushix.multisrc.madara.Madara

class MangaOwlOne : Madara("MangaOwl.one (unoriginal)", "https://mangaowl.one", "en") {
    override val useNewChapterEndpoint = false
    override val filterNonMangaItems = false
}
