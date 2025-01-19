package keiyoushi.extension.ar.noonscan

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class NoonScan : MangaThemesia(
    "نون سكان",
    "https://noonscan.com",
    "ar",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("ar")),
)
