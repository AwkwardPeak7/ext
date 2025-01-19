package keiyoushi.extension.tr.ragnarscans

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RagnarScans : Madara(
    "Ragnar Scans",
    "https://ragnarscans.com",
    "tr",
    dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override val useNewChapterEndpoint = true
}
