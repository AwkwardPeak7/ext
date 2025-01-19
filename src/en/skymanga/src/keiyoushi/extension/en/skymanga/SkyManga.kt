package keiyoushi.extension.en.skymanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.text.SimpleDateFormat
import java.util.Locale

class SkyManga : MangaThemesia(
    "Sky Manga",
    "https://skymanga.work",
    "en",
    "/manga-list",
    SimpleDateFormat("dd-MM-yyyy", Locale.US),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
