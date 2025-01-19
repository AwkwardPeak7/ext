package keiyoushi.extension.tr.webtoontr

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class WebtoonTR : Madara(
    "Webtoon TR",
    "https://webtoontr.net",
    "tr",
    SimpleDateFormat("dd/MM/yyy", Locale("tr")),
) {
    override val useNewChapterEndpoint = false
}
