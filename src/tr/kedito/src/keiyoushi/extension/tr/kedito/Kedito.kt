package keiyoushi.extension.tr.kedito

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Kedito : Madara(
    "Kedi.to",
    "https://kedi.to",
    "tr",
    SimpleDateFormat("dd MMM yyyy", Locale("tr")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val chapterUrlSuffix = ""
}
