package eu.kanade.tachiyomi.extension.es.rightdarkscan

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RightdarkScan : Madara(
    "Rightdark Scan",
    "https://rightdark-scan.com",
    "es",
    SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(2, 1)
        .build()

    override val useNewChapterEndpoint = true
}
