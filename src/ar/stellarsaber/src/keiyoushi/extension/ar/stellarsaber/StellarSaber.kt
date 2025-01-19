package keiyoushi.extension.ar.stellarsaber

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class StellarSaber : MangaThemesia(
    "StellarSaber",
    "https://stellarsaber.pro",
    "ar",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("ar")),
)
