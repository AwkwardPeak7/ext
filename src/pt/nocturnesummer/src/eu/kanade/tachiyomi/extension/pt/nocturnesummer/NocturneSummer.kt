package eu.kanade.tachiyomi.extension.pt.nocturnesummer

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class NocturneSummer : Madara(
    "Nocturne Summer",
    "https://nocsummer.com.br",
    "pt-BR",
    SimpleDateFormat("dd 'de' MMMMM 'de' yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()

    override val useNewChapterEndpoint = true
}
