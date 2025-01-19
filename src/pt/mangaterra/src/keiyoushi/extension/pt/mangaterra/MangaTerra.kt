package keiyoushi.extension.pt.mangaterra

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.terrascan.TerraScan
import java.util.concurrent.TimeUnit

class MangaTerra : TerraScan(
    "Manga Terra",
    "https://manga-terra.com",
    "pt-BR",
) {
    override val client = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
