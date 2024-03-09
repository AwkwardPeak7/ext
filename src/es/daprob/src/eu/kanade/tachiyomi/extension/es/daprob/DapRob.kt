package eu.kanade.tachiyomi.extension.es.daprob

import keiyoushix.multisrc.madara.Madara
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
