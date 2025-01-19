package keiyoushi.extension.tr.mangakoleji

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaKoleji : MangaThemesia(
    "Manga Koleji",
    "https://mangakoleji.com",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("tr")),
)
