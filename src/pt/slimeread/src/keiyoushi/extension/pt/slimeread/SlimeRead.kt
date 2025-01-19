package keiyoushi.extension.pt.slimeread

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.slimereadtheme.SlimeReadTheme

class SlimeRead : SlimeReadTheme(
    "SlimeRead",
    "https://slimeread.com",
    "pt-BR",
) {
    override fun headersBuilder() = super.headersBuilder()
        .add("Origin", baseUrl)

    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
