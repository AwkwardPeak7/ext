package eu.kanade.tachiyomi.extension.en.yakshascans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient

class YakshaScans : Madara(
    "YakshaScans",
    "https://yakshascans.com",
    "en",
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1)
        .build()

    override val useNewChapterEndpoint = true

    override val mangaDetailsSelectorDescription: String =
        "div.description-summary div.summary__content h3 + p, div.description-summary div.summary__content:not(:has(h3)), div.summary_content div.post-content_item > h5 + div, div.summary_content div.manga-excerpt"
}
