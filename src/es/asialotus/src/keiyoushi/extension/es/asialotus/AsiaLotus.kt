package keiyoushi.extension.es.asialotus

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class AsiaLotus : MangaThemesia(
    "Asia Lotus",
    "https://asialotuss.com",
    "es",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
