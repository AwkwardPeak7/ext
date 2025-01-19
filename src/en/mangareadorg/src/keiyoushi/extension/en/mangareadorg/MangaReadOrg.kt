package keiyoushi.extension.en.mangareadorg

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaReadOrg : Madara(
    "MangaRead.org",
    "https://www.mangaread.org",
    "en",
    SimpleDateFormat("dd.MM.yyy", Locale.US),
)
