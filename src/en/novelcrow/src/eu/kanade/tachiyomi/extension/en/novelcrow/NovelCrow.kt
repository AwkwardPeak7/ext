package eu.kanade.tachiyomi.extension.en.novelcrow

import keiyoushix.multisrc.madara.Madara

class NovelCrow : Madara("NovelCrow", "https://novelcrow.com", "en") {
    override val useNewChapterEndpoint = true
}
