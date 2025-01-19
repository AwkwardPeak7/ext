package keiyoushi.extension.es.manhwases

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ManhwasEs : Madara(
    "Manhwas.es",
    "https://manhwas.es",
    "es",
    dateFormat = SimpleDateFormat("MMM dd, yy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
