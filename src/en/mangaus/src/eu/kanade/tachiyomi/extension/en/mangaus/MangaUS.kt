package eu.kanade.tachiyomi.extension.en.mangaus

import keiyoushix.multisrc.madara.Madara

class MangaUS : Madara("MangaUS", "https://mangaus.xyz", "en") {
    override val pageListParseSelector = "img"
}
