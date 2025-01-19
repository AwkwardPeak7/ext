package keiyoushi.extension.pt.lscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class LScans : MangaThemesia(
    "L Scans",
    "https://lscans.com",
    "pt-BR",
) {
    // Moved from PeachScan to Mangathemsia
    override val versionId = 3

    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
