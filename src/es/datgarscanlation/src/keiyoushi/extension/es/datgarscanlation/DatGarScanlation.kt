package keiyoushi.extension.es.datgarscanlation

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class DatGarScanlation : ZeistManga(
    "Dat-Gar Scan",
    "https://datgarscanlation.blogspot.com",
    "es",
) {
    override val useNewChapterFeed = true
    override val hasFilters = true
    override val hasLanguageFilter = false

    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
