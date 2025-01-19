package keiyoushi.extension.en.mangakomi

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MangaKomi : Madara(
    "MangaKomi",
    "https://mangakomi.io",
    "en",
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 1, TimeUnit.SECONDS)
        .build()
}
