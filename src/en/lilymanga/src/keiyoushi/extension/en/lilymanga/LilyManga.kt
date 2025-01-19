package keiyoushi.extension.en.lilymanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class LilyManga : Madara("Lily Manga", "https://lilymanga.net", "en", SimpleDateFormat("yyyy-MM-dd", Locale.US)) {
    override val mangaSubString = "ys"
}
