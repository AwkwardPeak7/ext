package keiyoushi.extension.es.tresdaosscan

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class TresDaosScan : MangaThemesia(
    "Tres Daos Scan",
    "https://tresdaos.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val versionId = 4

    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()
}
