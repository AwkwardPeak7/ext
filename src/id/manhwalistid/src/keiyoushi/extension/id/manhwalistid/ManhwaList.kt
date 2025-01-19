package keiyoushi.extension.id.manhwalistid

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient

class ManhwaList : MangaThemesia(
    "Manhwa List",
    "https://manhwalist.xyz",
    "id",
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
