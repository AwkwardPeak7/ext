package keiyoushi.extension.bg.utsukushii

import eu.kanade.tachiyomi.network.GET
import keiyoushi.multisrc.mmrcms.MMRCMS
import okhttp3.Request

class Utsukushii : MMRCMS("Utsukushii", "https://utsukushii-bg.com", "bg") {
    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga-list", headers)
    }
}
