package eu.kanade.tachiyomi.extension.en.mangareadorg

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaReadOrg : Madara(
    "MangaRead.org",
    "https://www.mangaread.org",
    "en",
    SimpleDateFormat("dd.MM.yyy", Locale.US),
)
