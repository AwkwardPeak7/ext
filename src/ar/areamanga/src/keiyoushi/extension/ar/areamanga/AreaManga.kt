package keiyoushi.extension.ar.areamanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class AreaManga : MangaThemesia(
    "أريا مانجا",
    "https://ar.kenmanga.com",
    "ar",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("ar")),
)
