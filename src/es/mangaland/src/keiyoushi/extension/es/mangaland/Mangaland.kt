package keiyoushi.extension.es.mangaland

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class Mangaland : Madara(
    "Mangaland",
    "https://mangaland.net",
    "es",
    SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(2, 1, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true
    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
