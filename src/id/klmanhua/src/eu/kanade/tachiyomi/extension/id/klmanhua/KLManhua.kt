package eu.kanade.tachiyomi.extension.id.klmanhua

import keiyoushix.multisrc.zeistmanga.ZeistManga

class KLManhua : ZeistManga("KLManhua", "https://klmanhua.blogspot.com", "id") {

    override val hasFilters = true
    override val hasLanguageFilter = false
}
