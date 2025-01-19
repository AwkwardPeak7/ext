package keiyoushi.extension.en.spiderscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara

class SpiderScans : Madara(
    "Spider Scans",
    "https://spidyscans.xyz",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
