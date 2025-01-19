package keiyoushi.extension.en.neatmanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class NeatManga : Madara("NeatManga", "https://neatmanga.com", "en", SimpleDateFormat("dd MMM yyyy", Locale.US)) {
    override val useNewChapterEndpoint = true
}
