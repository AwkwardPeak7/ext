package keiyoushi.extension.tr.sereinscan

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class SereinScan : MangaThemesia(
    "Serein Scan",
    "https://sereinscan.com",
    "tr",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("tr")),
)
