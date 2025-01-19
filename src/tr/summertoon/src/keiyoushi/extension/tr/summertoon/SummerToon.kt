package keiyoushi.extension.tr.summertoon

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class SummerToon : MangaThemesia(
    "SummerToon",
    "https://summertoon.co",
    "tr",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("tr")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(1, 1)
        .build()

    override val seriesStatusSelector = ".tsinfo .imptdt:contains(Durum) i"
    override val seriesAuthorSelector = ".fmed b:contains(Yazar)+span"
}
