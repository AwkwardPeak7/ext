package keiyoushi.extension.id.ichiromanga

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class IchiroManga : MangaThemesia("IchiroManga", "https://ichiromanga.my.id", "id") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = false
}
