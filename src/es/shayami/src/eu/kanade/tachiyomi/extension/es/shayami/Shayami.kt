package eu.kanade.tachiyomi.extension.es.shayami

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Shayami : Madara(
    "Shayami",
    "https://shayami.com",
    "es",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
