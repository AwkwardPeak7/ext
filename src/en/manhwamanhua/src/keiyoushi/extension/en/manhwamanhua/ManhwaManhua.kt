package keiyoushi.extension.en.manhwamanhua

import keiyoushi.multisrc.madara.Madara

class ManhwaManhua : Madara("ManhwaManhua", "https://manhwamanhua.com", "en") {
    override val useNewChapterEndpoint = true
    override val filterNonMangaItems = false
}
