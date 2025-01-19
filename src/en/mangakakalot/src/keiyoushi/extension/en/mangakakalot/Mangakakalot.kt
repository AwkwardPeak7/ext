package keiyoushi.extension.en.mangakakalot

import keiyoushi.multisrc.mangabox.MangaBox
import okhttp3.Headers

class Mangakakalot : MangaBox("Mangakakalot", "https://mangakakalot.com", "en") {
    override fun headersBuilder(): Headers.Builder = super.headersBuilder().set("Referer", "https://manganato.com") // for covers
    override val simpleQueryPath = "search/story/"
    override fun searchMangaSelector() = "${super.searchMangaSelector()}, div.list-truyen-item-wrap"
}
