package keiyoushi.extension.en.mangasect

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.liliana.Liliana

class MangaSect : Liliana(
    "Manga Sect",
    "https://mangasect.net",
    "en",
    usesPostSearch = true,
) {
    override val client = super.client.newBuilder()
        .rateLimit(1)
        .build()
}
