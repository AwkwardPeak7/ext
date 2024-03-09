package eu.kanade.tachiyomi.extension.pt.flowermanga

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class FlowerManga : Madara(
    "Flower Manga",
    "https://flowermanga.com",
    "pt-BR",
    SimpleDateFormat("dd MMMMM yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
