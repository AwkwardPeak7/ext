package eu.kanade.tachiyomi.extension.tr.mangakeyfi

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaKeyfi : Madara(
    "Manga Keyfi",
    "https://mangakeyfi.net",
    "tr",
    SimpleDateFormat("d MMM yyy", Locale("tr")),
)
