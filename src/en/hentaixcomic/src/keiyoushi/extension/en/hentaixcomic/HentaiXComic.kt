package keiyoushi.extension.en.hentaixcomic

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class HentaiXComic : Madara(
    "HentaiXComic",
    "https://hentaixcomic.com",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US),
)
