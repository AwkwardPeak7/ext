package keiyoushi.extension.tr.afroditscans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class AfroditScans : MangaThemesia(
    "Afrodit Scans",
    "https://afroditscans.com",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyy", Locale("tr")),
)
