package keiyoushi.extension.th.popsmanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class PopsManga : MangaThemesia(
    "PopsManga",
    "https://popsmanga.com",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")),
)
