package keiyoushi.extension.pt.mangabr

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.terrascan.TerraScan
import java.util.concurrent.TimeUnit

class MangaBR : TerraScan(
    "Manga BR",
    "https://mangabr.net",
    "pt-BR",
) {
    override val client = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
