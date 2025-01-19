package keiyoushi.extension.tr.romantikmanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RomantikManga : Madara(
    "Romantik Manga",
    "https://romantikmanga.com",
    "tr",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("tr")),
) {
    override val useNewChapterEndpoint = true
}
