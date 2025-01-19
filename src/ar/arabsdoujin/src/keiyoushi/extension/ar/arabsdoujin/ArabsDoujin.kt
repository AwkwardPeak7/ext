package keiyoushi.extension.ar.arabsdoujin

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class ArabsDoujin : ZeistManga("Arabs Doujin", "https://www.arabsdoujin.online", "ar") {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val pageListSelector = "div.check-box"
}
