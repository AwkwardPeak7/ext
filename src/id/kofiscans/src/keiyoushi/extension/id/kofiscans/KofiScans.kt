package keiyoushi.extension.id.kofiscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class KofiScans : MangaThemesia(
    "Kofi Scans",
    "https://kofiscans.me",
    "id",
    "/manhwa",
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
