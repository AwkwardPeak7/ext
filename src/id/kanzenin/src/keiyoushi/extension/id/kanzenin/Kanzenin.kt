package keiyoushi.extension.id.kanzenin

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Kanzenin : MangaThemesia(
    "Kanzenin",
    "https://kanzenin.info",
    "id",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("id")),
)
