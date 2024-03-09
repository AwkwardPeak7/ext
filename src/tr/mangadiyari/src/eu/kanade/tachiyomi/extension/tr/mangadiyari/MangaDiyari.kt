package eu.kanade.tachiyomi.extension.tr.mangadiyari

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaDiyari : Madara("Manga Diyari", "https://manga-diyari.com", "tr", SimpleDateFormat("MMM dd, yyyy", Locale("tr"))) {
    override val useNewChapterEndpoint: Boolean = true
}
