package keiyoushi.extension.en.manhwafreake

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

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
