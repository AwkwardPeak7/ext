package keiyoushi.extension.pt.manganinja

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.text.SimpleDateFormat
import java.util.Locale

class MangaNinja : Madara(
    "Mangá Ninja",
    "https://manganinja.com",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {

    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()

    override val useLoadMoreRequest = LoadMoreStrategy.Never
}
