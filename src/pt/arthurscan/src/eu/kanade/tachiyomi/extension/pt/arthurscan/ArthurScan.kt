package eu.kanade.tachiyomi.extension.pt.arthurscan

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class ArthurScan : Madara(
    "Arthur Scan",
    "https://arthurscan.xyz",
    "pt-BR",
    SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true
}
