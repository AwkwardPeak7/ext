package eu.kanade.tachiyomi.extension.tr.ruyamanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RuyaManga : Madara(
    "Rüya Manga",
    "https://www.ruyamanga.com",
    "tr",
    SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
) {
    override val filterNonMangaItems = false
}
