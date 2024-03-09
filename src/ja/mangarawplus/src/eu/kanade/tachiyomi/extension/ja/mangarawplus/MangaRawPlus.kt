package eu.kanade.tachiyomi.extension.ja.mangarawplus

import keiyoushix.multisrc.madara.Madara

class MangaRawPlus : Madara("MANGARAW+", "https://mangarawplus.org", "ja") {
    override val mangaSubString = "sp"
    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
