package eu.kanade.tachiyomi.extension.th.mangamoon

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaMoon : MangaThemesia(
    "Manga-Moon",
    "https://manga-moons.net",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")),
)
