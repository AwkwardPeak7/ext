package keiyoushi.extension.en.holymanga

import keiyoushi.multisrc.fmreader.FMReader

class HolyManga : FMReader(
    "HolyManga",
    "https://w34.holymanga.net",
    "en",
) {
    override val versionId = 2

    override val chapterUrlSelector = ""
}
