package keiyoushi.extension.tr.mangacim

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Mangacim : MangaThemesia(
    "Mangacim",
    "https://www.mangacim.com",
    "tr",
    dateFormat = SimpleDateFormat("MMM d, yyy", Locale("tr")),
)
