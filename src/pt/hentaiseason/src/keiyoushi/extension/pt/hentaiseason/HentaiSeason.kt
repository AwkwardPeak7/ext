package keiyoushi.extension.pt.hentaiseason

import keiyoushi.multisrc.gattsu.Gattsu
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class HentaiSeason : Gattsu(
    "Hentai Season",
    "https://hentaiseason.com",
    "pt-BR",
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
