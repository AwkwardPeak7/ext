package keiyoushi.extension.en.anisascans

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class AnisaScans : Madara(
    "Anisa Scans",
    "https://anisascans.in",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true
}
