package eu.kanade.tachiyomi.extension.en.manhwahentaime

import keiyoushix.multisrc.madara.Madara

class ManhwahentaiMe : Madara("Manhwahentai.me", "https://manhwahentai.me", "en") {

    override val useNewChapterEndpoint: Boolean = true

    override val mangaSubString = "webtoon"
}
