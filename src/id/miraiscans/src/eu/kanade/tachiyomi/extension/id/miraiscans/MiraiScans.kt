package eu.kanade.tachiyomi.extension.id.miraiscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class MiraiScans : MangaThemesia("Mirai Scans", "https://miraiscans.com", "id", mangaUrlDirectory = "/komik") {

    override val hasProjectPage = true

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
