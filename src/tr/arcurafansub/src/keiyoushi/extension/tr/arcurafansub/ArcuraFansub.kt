package keiyoushi.extension.tr.arcurafansub

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class ArcuraFansub : MangaThemesia(
    "Arcura Fansub",
    "https://arcurafansub.com",
    "tr",
    "/seri",
    SimpleDateFormat("MMMM d, yyy", Locale("tr")),
)
