package eu.kanade.tachiyomi.extension.en.readfreecomics

import keiyoushix.multisrc.madara.Madara

class ReadFreeComics : Madara("ReadFreeComics", "https://readfreecomics.com", "en") {
    override val useNewChapterEndpoint = true
}
