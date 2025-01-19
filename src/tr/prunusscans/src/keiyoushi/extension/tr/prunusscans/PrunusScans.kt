package keiyoushi.extension.tr.prunusscans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class PrunusScans : MangaThemesia(
    "Prunus Scans",
    "https://prunusscans.com",
    "tr",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
