package keiyoushi.extension.id.dojingnet

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient

class DojingNet : MangaThemesia("Dojing.net", "https://dojing.net", "id") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
