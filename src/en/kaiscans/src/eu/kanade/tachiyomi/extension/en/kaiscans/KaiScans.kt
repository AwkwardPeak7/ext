package eu.kanade.tachiyomi.extension.en.kaiscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.mangathemesia.MangaThemesia

class KaiScans : MangaThemesia("Kai Scans", "https://kaiscans.org", "en") {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
