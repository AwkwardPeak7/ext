package keiyoushi.extension.tr.zenithscans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class ZenithScans : MangaThemesia(
    "Zenith Scans",
    "https://zenithscans.com",
    "tr",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("tr")),
)
