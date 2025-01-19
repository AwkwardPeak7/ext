package keiyoushi.extension.ar.arabsdoujin

import keiyoushi.multisrc.zeistmanga.ZeistManga
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class ArabsDoujin : ZeistManga("Arabs Doujin", "https://www.arabsdoujin.online", "ar") {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val pageListSelector = "div.check-box"
}
