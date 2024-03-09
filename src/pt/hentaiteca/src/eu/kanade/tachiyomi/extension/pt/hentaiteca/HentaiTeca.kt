package eu.kanade.tachiyomi.extension.pt.hentaiteca

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class HentaiTeca : Madara(
    "Hentai Teca",
    "https://hentaiteca.net",
    "pt-BR",
    SimpleDateFormat("MMMMM dd, yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
