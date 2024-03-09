package eu.kanade.tachiyomi.extension.tr.mangawow

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaWOW : Madara(
    "MangaWOW",
    "https://mangawow.org",
    "tr",
    SimpleDateFormat("MMMM dd, yyyy", Locale("tr")),
)
