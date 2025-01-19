package keiyoushi.extension.tr.mindafansub

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MindaFansub : Madara(
    "Minda Fansub",
    "https://mindafansub.online",
    "tr",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("tr")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true
}
