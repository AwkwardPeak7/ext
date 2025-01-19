package keiyoushi.extension.id.kumapoi

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient

class KumaPoi : MangaThemesia("KumaPoi", "https://kumapoi.info", "id") {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
