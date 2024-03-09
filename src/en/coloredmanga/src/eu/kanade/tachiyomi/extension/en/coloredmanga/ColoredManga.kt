package eu.kanade.tachiyomi.extension.en.coloredmanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ColoredManga : Madara(
    "Colored Manga",
    "https://coloredmanga.com",
    "en",
    dateFormat = SimpleDateFormat("dd-MMM", Locale.ENGLISH),
) {
    override val useNewChapterEndpoint = true
}
