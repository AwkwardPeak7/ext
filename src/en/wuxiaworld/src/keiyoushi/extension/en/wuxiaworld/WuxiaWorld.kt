package keiyoushi.extension.en.wuxiaworld

import keiyoushi.multisrc.madara.Madara

class WuxiaWorld : Madara("WuxiaWorld", "https://wuxiaworld.site", "en") {
    override val mangaSubString = "novel"
    override val useNewChapterEndpoint = true
}
