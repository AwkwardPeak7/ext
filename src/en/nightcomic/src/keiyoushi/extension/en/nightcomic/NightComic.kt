package keiyoushi.extension.en.nightcomic

import keiyoushi.multisrc.madara.Madara

class NightComic : Madara("Night Comic", "https://nightcomic.com", "en") {
    override val useNewChapterEndpoint = true
    override val mangaDetailsSelectorAuthor = "div.manga-authors > a"
}
