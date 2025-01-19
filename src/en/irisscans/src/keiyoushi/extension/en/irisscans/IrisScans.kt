package keiyoushi.extension.en.irisscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class IrisScans : MangaThemesia(
    "Iris Scans",
    "https://irisscans.xyz",
    "en",
) {
    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()
}
