package keiyoushi.extension.id.kishisan

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class Kishisan : ZeistManga("Kishisan", "https://www.kishisan.site", "id") {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
