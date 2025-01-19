package keiyoushi.extension.es.scambertraslator

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ScamberTraslator : Madara(
    "ScamberTraslator",
    "https://visorscamber-scans.com",
    "es",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es")),
) {
    override val useNewChapterEndpoint = true
}
