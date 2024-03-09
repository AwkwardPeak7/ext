package eu.kanade.tachiyomi.extension.ar.stellarsaber

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class StellarSaber : MangaThemesia(
    "StellarSaber",
    "https://stellarsaber.pro",
    "ar",
    dateFormat = SimpleDateFormat("MMMMM dd, yyyy", Locale("ar")),
)
