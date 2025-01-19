package keiyoushi.extension.tr.mangasehrinet

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaSehriNet : Madara(
    "Manga Şehri.net",
    "https://manga-sehri.net",
    "tr",
    dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.AutoDetect
    override val useNewChapterEndpoint = false
}
