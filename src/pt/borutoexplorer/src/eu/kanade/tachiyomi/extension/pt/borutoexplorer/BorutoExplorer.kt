package eu.kanade.tachiyomi.extension.pt.borutoexplorer

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.madara.Madara
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class BorutoExplorer : Madara(
    "Boruto Explorer",
    "https://leitor.borutoexplorer.com.br",
    "pt-BR",
    SimpleDateFormat("dd 'de' MMMMM 'de' yyyy", Locale("pt", "BR")),
) {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(1, 2, TimeUnit.SECONDS)
        .build()
}
