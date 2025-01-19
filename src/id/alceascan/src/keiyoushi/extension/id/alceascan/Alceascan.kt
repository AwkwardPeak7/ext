package keiyoushi.extension.id.alceascan

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient

class Alceascan : MangaThemesia("Alceascan", "https://alceacomic.my.id", "id") {

    // Website theme changed from zManga to WPMangaThemesia.
    override val versionId = 2

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(20, 4)
        .build()

    override val hasProjectPage = true
}
