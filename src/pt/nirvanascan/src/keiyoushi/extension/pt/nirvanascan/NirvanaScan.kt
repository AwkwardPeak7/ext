package keiyoushi.extension.pt.nirvanascan

import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import java.text.SimpleDateFormat
import java.util.Locale

class NirvanaScan : Madara(
    "Nirvana Scan",
    "https://nirvanascan.com",
    "pt-BR",
    SimpleDateFormat("MMM dd, yyyy", Locale("pt", "BR")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val useNewChapterEndpoint = true
}
