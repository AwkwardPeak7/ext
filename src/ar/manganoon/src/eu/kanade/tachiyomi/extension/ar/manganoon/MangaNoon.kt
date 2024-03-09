package eu.kanade.tachiyomi.extension.ar.manganoon

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaNoon : MangaThemesia(
    "مانجا نون",
    "https://manjanoon.net",
    "ar",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("ar")),
)
