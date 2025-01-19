package keiyoushi.extension.pt.pussytoons

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class PussyToons : Madara(
    "PussyToons",
    "https://pussy.sussytoons.com",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("pt", "BR")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true

    override fun popularMangaSelector() = ".main-col-inner div.page-item-detail"
}
