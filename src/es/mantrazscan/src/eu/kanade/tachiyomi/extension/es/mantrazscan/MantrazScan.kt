package eu.kanade.tachiyomi.extension.es.mantrazscan

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MantrazScan : Madara(
    "Mantraz Scan",
    "https://mantrazscan.com",
    "es",
    SimpleDateFormat("dd/MM/yyyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
