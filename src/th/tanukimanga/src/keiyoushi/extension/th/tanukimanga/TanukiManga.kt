package keiyoushi.extension.th.tanukimanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class TanukiManga : MangaThemesia(
    "Tanuki-Manga",
    "https://www.tanuki-manga.com",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")),
)
