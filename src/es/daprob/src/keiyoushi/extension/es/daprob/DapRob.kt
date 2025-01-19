package keiyoushi.extension.es.daprob

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class DapRob : Madara(
    "DapRob",
    "https://daprob.com",
    "es",
    dateFormat = SimpleDateFormat("dd/MM/yyy", Locale.ROOT),
) {
    override val useNewChapterEndpoint = true
}
