package keiyoushi.extension.id.komikmama

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class KomikMama : MangaThemesia(
    "KomikMama",
    "https://komikmama.org",
    "id",
    "/komik",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
)
