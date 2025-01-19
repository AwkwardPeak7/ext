package keiyoushi.extension.en.manhwafreake

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit

class ManhwaFreake : MangaThemesia(
    "Manhwa Freake",
    "https://manhwafreake.com",
    "en",
    mangaUrlDirectory = "/series",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
