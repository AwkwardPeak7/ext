package keiyoushi.extension.tr.moondaisyscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MoonDaisyScans : MangaThemesia(
    "Moon Daisy Scans",
    "https://moondaisyscans.biz",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyy", Locale("tr")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()
}
