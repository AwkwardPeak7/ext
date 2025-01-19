package keiyoushi.extension.pt.cerisescans

import keiyoushi.multisrc.peachscan.PeachScan
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class CeriseScan : PeachScan(
    "Cerise Scan",
    "https://cerise.leitorweb.com",
    "pt-BR",
) {
    override val versionId: Int = 2

    override val client = super.client.newBuilder()
        .rateLimit(1, 2)
        .build()
}
