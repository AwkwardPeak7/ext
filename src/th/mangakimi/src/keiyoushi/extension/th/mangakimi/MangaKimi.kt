package keiyoushi.extension.th.mangakimi

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MangaKimi : MangaThemesia(
    "MangaKimi",
    "https://www.mangakimi.com",
    "th",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("th")).apply {
        timeZone = TimeZone.getTimeZone("Asia/Bangkok")
    },
)
