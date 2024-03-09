package eu.kanade.tachiyomi.extension.tr.mangawt

import keiyoushix.multisrc.madara.Madara

class MangaWT : Madara(
    "MangaWT",
    "https://mangawt.com",
    "tr",
) {
    override val useNewChapterEndpoint = true
}
