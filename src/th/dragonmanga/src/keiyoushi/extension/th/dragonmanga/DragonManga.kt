package keiyoushi.extension.th.dragonmanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class DragonManga : MangaThemesia(
    "DragonManga",
    "https://www.dragon-manga.com",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")),
)
