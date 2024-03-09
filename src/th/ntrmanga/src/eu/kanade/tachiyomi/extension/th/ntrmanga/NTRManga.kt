package eu.kanade.tachiyomi.extension.th.ntrmanga

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class NTRManga : MangaThemesia(
    "NTR-Manga",
    "https://www.ntr-manga.com",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")),
)
