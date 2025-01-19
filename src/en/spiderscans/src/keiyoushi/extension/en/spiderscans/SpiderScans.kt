package keiyoushi.extension.en.spiderscans

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class SpiderScans : Madara(
    "Spider Scans",
    "https://spidyscans.xyz",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
