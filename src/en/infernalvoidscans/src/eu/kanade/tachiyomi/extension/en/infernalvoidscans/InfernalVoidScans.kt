package eu.kanade.tachiyomi.extension.en.infernalvoidscans

import keiyoushix.multisrc.mangathemesia.MangaThemesia

class InfernalVoidScans : MangaThemesia("Infernal Void Scans", "https://hivescans.com", "en") {
    override val pageSelector = "div#readerarea > p > img"
}
