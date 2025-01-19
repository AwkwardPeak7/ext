package keiyoushi.extension.en.mangaowlio

import keiyoushi.multisrc.madara.Madara

class MangaOwlIo : Madara("MangaOwl.io (unoriginal)", "https://mangaowl.io", "en") {
    override val useNewChapterEndpoint = true
}
