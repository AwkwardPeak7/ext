package keiyoushi.extension.es.yaoimanga

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara
import okhttp3.OkHttpClient

class YaoiManga : Madara(
    "Yaoi Manga",
    "https://yaoimanga.es",
    "es",
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Never
}
