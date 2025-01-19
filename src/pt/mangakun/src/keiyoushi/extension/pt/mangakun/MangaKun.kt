package keiyoushi.extension.pt.mangakun

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class MangaKun : MangaThemesia(
    "Mangá Kun",
    "https://mangakun.com.br",
    "pt-BR",
) {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
