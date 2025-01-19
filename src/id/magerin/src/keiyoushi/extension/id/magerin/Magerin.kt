package keiyoushi.extension.id.magerin

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class Magerin : ZeistManga("Magerin", "https://www.magerin.com", "id") {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
