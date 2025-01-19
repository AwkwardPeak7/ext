package keiyoushi.extension.es.novatoscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.zeistmanga.ZeistManga

class NovatoScans : ZeistManga(
    "Novato Scans",
    "https://www.novatoscans.top",
    "es",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val pageListSelector = "div.check-box"
}
