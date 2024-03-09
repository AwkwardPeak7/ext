package eu.kanade.tachiyomi.extension.th.kingsmanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class KingsManga : Madara(
    "Kings-Manga",
    "https://www.kings-manga.co",
    "th",
    dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("th")),
) {
    override val useNewChapterEndpoint = false
}
