package keiyoushi.extension.en.lhtranslation

import keiyoushi.multisrc.madara.Madara

class LHTranslation : Madara("LHTranslation", "https://lhtranslation.net", "en") {
    override val versionId = 2
    override val useNewChapterEndpoint = true
}
