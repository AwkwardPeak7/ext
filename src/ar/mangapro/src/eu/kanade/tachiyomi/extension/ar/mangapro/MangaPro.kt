package eu.kanade.tachiyomi.extension.ar.mangapro

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaPro : MangaThemesia(
    "Manga Pro",
    "https://mangapro.pro",
    "ar",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("ar")),
) {
    override val versionId = 3
}
