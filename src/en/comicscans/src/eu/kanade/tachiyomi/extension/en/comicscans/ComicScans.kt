package eu.kanade.tachiyomi.extension.en.comicscans

import keiyoushix.multisrc.madara.Madara

class ComicScans : Madara("Comic Scans", "https://www.comicscans.org", "en") {
    override val useNewChapterEndpoint = true
}
