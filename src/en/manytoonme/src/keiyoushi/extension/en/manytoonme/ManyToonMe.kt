package keiyoushi.extension.en.manytoonme

import keiyoushi.multisrc.madara.Madara

class ManyToonMe : Madara("ManyToon.me", "https://manytoon.me", "en") {

    override val mangaSubString = "comic"

    override val useNewChapterEndpoint: Boolean = true
}
