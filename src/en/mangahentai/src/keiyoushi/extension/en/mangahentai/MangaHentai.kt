package keiyoushi.extension.en.mangahentai

import keiyoushi.multisrc.madara.Madara

class MangaHentai : Madara("Manga Hentai", "https://mangahentai.me", "en") {
    override val mangaSubString = "manga-hentai"

    override val useNewChapterEndpoint: Boolean = true
}
