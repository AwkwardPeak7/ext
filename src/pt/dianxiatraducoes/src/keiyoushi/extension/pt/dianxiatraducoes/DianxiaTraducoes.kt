package keiyoushi.extension.pt.dianxiatraducoes

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class DianxiaTraducoes : Madara(
    "Dianxia Traduções",
    "https://dianxiatrads.com",
    "pt-BR",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false
}
