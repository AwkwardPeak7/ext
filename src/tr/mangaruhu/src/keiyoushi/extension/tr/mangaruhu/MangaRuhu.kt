package keiyoushi.extension.tr.mangaruhu

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaRuhu : Madara(
    "Manga Ruhu",
    "https://mangaruhu.com",
    "tr",
    SimpleDateFormat("d MMMM yyyy", Locale("tr")),
) {
    override val filterNonMangaItems = false
}
