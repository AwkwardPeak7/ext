package eu.kanade.tachiyomi.extension.pt.nexoscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NexoScans : Madara(
    "Nexo Scans",
    "https://nexoscans.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale.US),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true

    override val chapterUrlSelector = "a:not(div.chapter-thumbnail a)"
}
