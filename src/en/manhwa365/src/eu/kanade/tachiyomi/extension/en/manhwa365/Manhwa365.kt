package eu.kanade.tachiyomi.extension.en.manhwa365

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Manhwa365 : Madara(
    "Manhwa365",
    "https://manhwa365.com",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US),
)
