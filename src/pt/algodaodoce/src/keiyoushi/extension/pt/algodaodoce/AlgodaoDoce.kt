package keiyoushi.extension.pt.algodaodoce

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class AlgodaoDoce : Madara(
    "Algodão Doce",
    "https://xn--algododoce-j5a.com",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("pt", "BR")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = true
}
