package keiyoushi.extension.en.quantumscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.keyoapp.Keyoapp
import java.util.concurrent.TimeUnit

class QuantumScans : Keyoapp(
    "Quantum Scans",
    "https://quantumscans.org",
    "en",
) {
    // Moved from Mangathemsia to Keyoapp
    override val versionId = 2

    override val client = super.client.newBuilder()
        .rateLimit(3, 1, TimeUnit.SECONDS)
        .build()
}
