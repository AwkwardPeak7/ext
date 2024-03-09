package eu.kanade.tachiyomi.extension.id.lianscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class LianScans : MangaThemesia("LianScans", "https://www.lianscans.my.id", "id") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
