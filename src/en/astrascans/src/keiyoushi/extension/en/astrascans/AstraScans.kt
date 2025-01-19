package keiyoushi.extension.en.astrascans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

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
