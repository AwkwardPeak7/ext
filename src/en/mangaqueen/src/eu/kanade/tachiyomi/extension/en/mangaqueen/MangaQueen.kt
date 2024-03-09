package eu.kanade.tachiyomi.extension.en.mangaqueen

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaQueen : Madara(
    "Manga Queen",
    "https://mangaqueen.net",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US),
)
