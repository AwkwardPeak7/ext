package keiyoushi.extension.es.shadowmangas

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class ShadowMangas : MangaThemesia(
    "Shadow Mangas",
    "https://shadowmangas.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
)
