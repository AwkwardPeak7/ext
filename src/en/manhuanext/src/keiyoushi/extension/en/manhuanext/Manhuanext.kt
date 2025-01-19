package keiyoushi.extension.en.manhuanext

import keiyoushi.multisrc.madara.Madara

class Manhuanext : Madara(
    "Manhuanext",
    "https://manhuanext.com",
    "en",
) {
    override val useNewChapterEndpoint = true
}
