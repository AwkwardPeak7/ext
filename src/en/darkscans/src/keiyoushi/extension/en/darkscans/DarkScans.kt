package keiyoushi.extension.en.darkscans

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import okhttp3.OkHttpClient

class DarkScans : Madara("Dark Scans", "https://darkscans.net", "en") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(20, 4)
        .build()

    override val mangaSubString = "mangas"

    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true

    override val filterNonMangaItems = false
}
