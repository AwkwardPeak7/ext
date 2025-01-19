package keiyoushi.extension.tr.asurascanstr

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class AsuraScansTR : Madara(
    "Asura Scans TR",
    "https://asurascans.com.tr",
    "tr",
    dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false

    override val altNameSelector = ".post-content_item:contains(Diğer Adlar) .summary-content"
}
