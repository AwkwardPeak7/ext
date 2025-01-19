package keiyoushi.extension.en.kumascans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class KumaScans : MangaThemesia("Kuma Scans (Kuma Translation)", "https://kumascans.com", "en") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
