package keiyoushi.extension.ar.manhatok

import keiyoushi.multisrc.zeistmanga.ZeistManga
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class Manhatok : ZeistManga("Manhatok", "https://manhatok.blogspot.com", "ar") {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
