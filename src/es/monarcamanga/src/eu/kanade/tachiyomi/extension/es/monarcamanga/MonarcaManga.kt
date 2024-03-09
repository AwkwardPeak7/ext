package eu.kanade.tachiyomi.extension.es.monarcamanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MonarcaManga : Madara(
    "MonarcaManga",
    "https://monarcamanga.com",
    "es",
    SimpleDateFormat("MMM d, yyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
