package keiyoushi.extension.id.manhwalistorg

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class ManhwalistOrg : MangaThemesia(
    "Manhwalist.org",
    "https://manhwalist.org",
    "id",
    mangaUrlDirectory = "/manhwa",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
)
