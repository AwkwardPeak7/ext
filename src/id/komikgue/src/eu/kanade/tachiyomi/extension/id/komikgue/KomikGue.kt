package eu.kanade.tachiyomi.extension.id.komikgue

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class KomikGue : Madara(
    "Komik Gue",
    "https://komikgue.pro",
    "id",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
) {
    override val useNewChapterEndpoint = true
}
