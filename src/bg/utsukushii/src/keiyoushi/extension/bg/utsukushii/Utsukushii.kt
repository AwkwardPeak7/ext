package keiyoushi.extension.bg.utsukushii

import keiyoushi.multisrc.mmrcms.MMRCMS
import eu.kanade.tachiyomi.network.GET
import okhttp3.Request

class Utsukushii : MMRCMS("Utsukushii", "https://utsukushii-bg.com", "bg") {
    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga-list", headers)
    }
}
