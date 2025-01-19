package keiyoushi.extension.id.komiksay

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class KomikSay : MangaThemesia(
    "Komik Say",
    "https://komiksay.info",
    "id",
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
