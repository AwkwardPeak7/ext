package eu.kanade.tachiyomi.extension.en.mangaryu

import keiyoushix.multisrc.madara.Madara

class Mangaryu : Madara("Mangaryu", "https://mangaryu.com", "en") {
    override val useNewChapterEndpoint = false
}
