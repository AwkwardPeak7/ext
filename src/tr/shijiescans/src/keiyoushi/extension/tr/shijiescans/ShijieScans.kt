package keiyoushi.extension.tr.shijiescans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class ShijieScans : MangaThemesia(
    "Shijie Scans",
    "https://shijiescans.com",
    "tr",
    "/seri",
    SimpleDateFormat("MMM d, yyy", Locale("tr")),
)
