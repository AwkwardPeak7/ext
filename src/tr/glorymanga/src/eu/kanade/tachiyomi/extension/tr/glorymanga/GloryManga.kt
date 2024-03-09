package eu.kanade.tachiyomi.extension.tr.glorymanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class GloryManga : Madara(
    "Glory Manga",
    "https://glorymanga.com",
    "tr",
    dateFormat = SimpleDateFormat("dd/MM/yyy", Locale.ROOT),
) {
    override val useNewChapterEndpoint = true
}
