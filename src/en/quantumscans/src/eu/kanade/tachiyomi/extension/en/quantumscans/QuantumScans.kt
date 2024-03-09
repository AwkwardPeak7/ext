package eu.kanade.tachiyomi.extension.en.quantumscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushix.multisrc.mangathemesia.MangaThemesia
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class QuantumScans : MangaThemesia("Quantum Scans", "https://readers-point.space", "en", "/series") {

    override val client: OkHttpClient = super.client.newBuilder()
        .rateLimit(12, 3, TimeUnit.SECONDS)
        .build()
}
