package keiyoushi.extension.en.hentai20

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient

class Hentai20 : MangaThemesia("Hentai20", "https://hentai20.io", "en") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1)
        .build()
}
