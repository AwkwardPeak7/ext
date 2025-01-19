package keiyoushi.extension.id.otsugami

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class Otsugami : MangaThemesia("Otsugami", "https://otsugami.id", "id") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val hasProjectPage = true
}
