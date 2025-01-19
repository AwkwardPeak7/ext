package keiyoushi.extension.tr.merlinscans

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MerlinScans : MangaThemesia(
    "Merlin Scans",
    "https://merlinscans.com",
    "tr",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("tr")),
)
