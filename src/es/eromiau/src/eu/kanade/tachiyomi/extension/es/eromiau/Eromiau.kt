package eu.kanade.tachiyomi.extension.es.eromiau

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Eromiau : Madara(
    "Eromiau",
    "https://www.eromiau.com",
    "es",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
