package keiyoushi.extension.pt.limitedtimeproject

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.text.SimpleDateFormat
import java.util.Locale

class LimitedTimeProject : Madara(
    "Limited Time Project",
    "https://limitedtimeproject.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val mangaSubString = "manhwas"

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true
}
