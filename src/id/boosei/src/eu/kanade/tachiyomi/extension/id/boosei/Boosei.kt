package eu.kanade.tachiyomi.extension.id.boosei

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class Boosei : MangaThemesia("Boosei", "https://boosei.net", "id") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(4)
        .build()

    override val hasProjectPage = true
}
