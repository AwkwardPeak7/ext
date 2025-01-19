package keiyoushi.extension.tr.imparatormanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ImparatorManga : Madara(
    "İmparator Manga",
    "https://www.imparatormanga.com",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("tr")),
) {
    override val useNewChapterEndpoint = true
}
