package keiyoushi.extension.tr.gaiatoon

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Gaiatoon : MangaThemesia(
    "Gaiatoon",
    "https://gaiatoon.com",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyy", Locale("tr")),
)
