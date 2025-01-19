package keiyoushi.extension.es.barmanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class BarManga : Madara(
    "BarManga",
    "https://barmanga.com",
    "es",
    SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val mangaDetailsSelectorDescription = "div.flamesummary > div.manga-excerpt"
}
