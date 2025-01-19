package keiyoushi.extension.en.toongod

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ToonGod : Madara("ToonGod", "https://www.toongod.org", "en", SimpleDateFormat("d MMM yyyy", Locale.US)) {
    override val mangaSubString = "webtoons"
    override val useNewChapterEndpoint = false
}
