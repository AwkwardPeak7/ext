package keiyoushi.extension.es.lazonadellirio

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class LaZonadelLirio : MangaThemesia(
    "La Zona del Lirio",
    "https://lazonadellirio.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
)
