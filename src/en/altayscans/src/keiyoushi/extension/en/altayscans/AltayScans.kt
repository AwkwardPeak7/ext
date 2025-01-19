package keiyoushi.extension.en.altayscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class AltayScans : MangaThemesia(
    "Altay Scans",
    "https://altayscans.com",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
