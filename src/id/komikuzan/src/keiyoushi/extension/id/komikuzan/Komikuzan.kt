package keiyoushi.extension.id.komikuzan

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Komikuzan : Madara(
    "Komikuzan",
    "https://komikuzan.com",
    "id",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("id")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true
}
