package keiyoushi.extension.pt.apecomics

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ApeComics : Madara(
    "ApeComics",
    "https://apecomics.net",
    "pt-BR",
    SimpleDateFormat("MMMM dd, yyyy", Locale("pt", "BR")),
) {
    override val useNewChapterEndpoint = true
}
