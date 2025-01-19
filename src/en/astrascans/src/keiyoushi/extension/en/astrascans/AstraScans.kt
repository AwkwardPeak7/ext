package keiyoushi.extension.en.astrascans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class AstraScans : MangaThemesia(
    "Astra Scans",
    "https://astrascans.org",
    "en",
    "/series",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
