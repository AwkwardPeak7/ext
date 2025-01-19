package keiyoushi.extension.en.comickiba

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.liliana.Liliana

class Manhuagold : Liliana(
    "Manhuagold",
    "https://manhuagold.top",
    "en",
    usesPostSearch = true,
) {
    // MangaReader -> Liliana
    override val versionId = 2

    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
