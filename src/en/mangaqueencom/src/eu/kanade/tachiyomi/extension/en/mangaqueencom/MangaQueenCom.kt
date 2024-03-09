package eu.kanade.tachiyomi.extension.en.mangaqueencom

import keiyoushix.multisrc.madara.Madara

class MangaQueenCom : Madara("Manga Queen.com", "https://mangaqueen.com", "en") {
    override val useNewChapterEndpoint = false
}
