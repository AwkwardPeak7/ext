package keiyoushi.extension.es.inmortalscan

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class InmortalScan : Madara(
    "Inmortal Scan",
    "https://scaninmortal.com",
    "es",
    SimpleDateFormat("MMM dd, yyyy", Locale("es")),
) {
    override val mangaSubString = "mg"

    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true
}
