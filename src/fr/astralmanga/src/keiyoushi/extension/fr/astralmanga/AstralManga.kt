package keiyoushi.extension.fr.astralmanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class AstralManga : Madara("AstralManga", "https://astral-manga.fr", "fr", dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)) {
    override val useNewChapterEndpoint = true
}
