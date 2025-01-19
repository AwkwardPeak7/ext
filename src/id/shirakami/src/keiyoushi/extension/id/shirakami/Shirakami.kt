package keiyoushi.extension.id.shirakami

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Shirakami : MangaThemesia(
    "Shirakami",
    "https://shirakami.xyz",
    "id",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
)
