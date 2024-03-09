package eu.kanade.tachiyomi.extension.pt.fayscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class FayScans : Madara(
    "Fay Scans",
    "https://fayscans.net",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true
}
