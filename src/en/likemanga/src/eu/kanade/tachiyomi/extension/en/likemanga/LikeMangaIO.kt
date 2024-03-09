package eu.kanade.tachiyomi.extension.en.likemanga

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.likemanga.LikeManga

class LikeMangaIO : LikeManga("LikeManga", "https://likemanga.io", "en") {
    override val client = super.client.newBuilder()
        .rateLimit(1, 2)
        .build()
}
