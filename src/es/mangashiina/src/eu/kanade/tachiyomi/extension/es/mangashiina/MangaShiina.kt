package eu.kanade.tachiyomi.extension.es.mangashiina

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaShiina : MangaThemesia(
    "MangaShiina",
    "https://mangashiina.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
)
