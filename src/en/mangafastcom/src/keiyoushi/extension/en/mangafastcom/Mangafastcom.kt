package keiyoushi.extension.en.mangafastcom

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Mangafastcom : Madara(
    "Manga-fast.com",
    "https://manga-fast.com",
    "en",
    dateFormat = SimpleDateFormat("d MMMM'،' yyyy", Locale.US),
)
