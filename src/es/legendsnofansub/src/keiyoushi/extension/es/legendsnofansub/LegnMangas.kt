package keiyoushi.extension.es.legendsnofansub

import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import keiyoushi.multisrc.madara.Madara
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class LegnMangas : Madara(
    "LegnMangas",
    "https://legnmangas.com",
    "es",
    SimpleDateFormat("dd/MM/yyyy", Locale("es")),
) {
    override val id = 9078720153732517844

    override val client = super.client.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 2, 1, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true
    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
