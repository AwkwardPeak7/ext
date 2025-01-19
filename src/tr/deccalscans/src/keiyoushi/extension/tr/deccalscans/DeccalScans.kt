package keiyoushi.extension.tr.deccalscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class DeccalScans : Madara(
    "Deccal Scans",
    "https://deccalscans.net",
    "tr",
    SimpleDateFormat("dd MMM yyyy", Locale("tr")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useNewChapterEndpoint = true

    override val mangaDetailsSelectorDescription = ".manga-summary p"
    override val mangaDetailsSelectorAuthor = ".manga-authors a"
}
