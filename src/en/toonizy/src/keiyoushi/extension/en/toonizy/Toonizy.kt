package keiyoushi.extension.en.toonizy

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Toonizy : Madara(
    "Toonizy",
    "https://toonizy.com",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yy", Locale.ENGLISH),
) {
    override val useNewChapterEndpoint = true
}
