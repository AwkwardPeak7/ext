package eu.kanade.tachiyomi.extension.en.webtooncity

import keiyoushix.multisrc.madara.Madara

class WebtoonCity : Madara("Webtoon City", "https://webtooncity.com", "en") {
    override val useNewChapterEndpoint = false
    override val mangaSubString = "webtoon"
}
