package keiyoushi.extension.pt.sinensis

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.peachscan.PeachScan

class SinensisScan : PeachScan(
    "Sinensis Scan",
    "https://sinensis.leitorweb.com",
    "pt-BR",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
