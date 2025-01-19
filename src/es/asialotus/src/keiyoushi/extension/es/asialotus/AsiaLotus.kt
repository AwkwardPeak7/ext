package keiyoushi.extension.es.asialotus

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class AsiaLotus : MangaThemesia(
    "Asia Lotus",
    "https://asialotuss.com",
    "es",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
