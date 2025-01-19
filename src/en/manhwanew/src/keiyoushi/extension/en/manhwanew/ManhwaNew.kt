package keiyoushi.extension.en.manhwanew

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ManhwaNew : Madara(
    "ManhwaNew",
    "https://manhwanew.com",
    "en",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val useNewChapterEndpoint = true
    override val filterNonMangaItems = false
}
