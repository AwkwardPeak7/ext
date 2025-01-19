package keiyoushi.extension.en.firstkissmanganet

import keiyoushi.multisrc.madara.Madara

class FirstKissMangaNet : Madara(
    "1st-Kiss Manga.net",
    "https://1st-kissmanga.net",
    "en",
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false
}
