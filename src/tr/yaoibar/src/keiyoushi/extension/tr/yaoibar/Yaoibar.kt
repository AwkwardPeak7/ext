package keiyoushi.extension.tr.yaoibar

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Yaoibar : Madara(
    "Yaoibar",
    "https://yaoibar.gay",
    "tr",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("tr")),
) {
    override val useNewChapterEndpoint: Boolean = true
}
