package eu.kanade.tachiyomi.extension.ar.mangarose

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaRose : Madara(
    "Manga Rose",
    "https://mangarose.net",
    "ar",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("ar")),
)
