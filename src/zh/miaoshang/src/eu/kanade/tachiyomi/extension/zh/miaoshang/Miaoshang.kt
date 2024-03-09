package eu.kanade.tachiyomi.extension.zh.miaoshang

import eu.kanade.tachiyomi.network.interceptor.rateLimitHost
import keiyoushix.multisrc.mccms.MCCMS
import keiyoushix.multisrc.mccms.MCCMSConfig
import okhttp3.HttpUrl.Companion.toHttpUrl

class Miaoshang : MCCMS(
    "喵上漫画",
    "https://www.miaoshangmanhua.com",
    "zh",
    MCCMSConfig(
        textSearchOnlyPageOne = true,
        lazyLoadImageAttr = "data-src",
    ),
) {
    override val client = network.cloudflareClient.newBuilder()
        .rateLimitHost(baseUrl.toHttpUrl(), 2)
        .build()
}
