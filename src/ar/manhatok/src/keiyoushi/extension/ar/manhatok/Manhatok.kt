package keiyoushi.extension.ar.manhatok

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class Manhatok : ZeistManga("Manhatok", "https://manhatok.blogspot.com", "ar") {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
