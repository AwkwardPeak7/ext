package keiyoushi.extension.es.ryujinmanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class RyujinManga : MangaThemesia(
    "RyujinManga",
    "https://ryujinmanga.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
)
