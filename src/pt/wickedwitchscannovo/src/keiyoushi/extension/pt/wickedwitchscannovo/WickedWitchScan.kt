package keiyoushi.extension.pt.wickedwitchscannovo

import keiyoushi.multisrc.peachscan.PeachScan
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.util.concurrent.TimeUnit

class WickedWitchScan : PeachScan("Wicked Witch Scan", "https://wicked-witch-scan.com", "pt-BR") {
    // Source changed from Madara to PeachScan
    override val versionId = 2

    override val client = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
