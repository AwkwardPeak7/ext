package keiyoushi.extension.tr.prunusscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class PrunusScans : MangaThemesia(
    "Prunus Scans",
    "https://prunusscans.com",
    "tr",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
