package keiyoushi.extension.ar.yonabar

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class YonaBar : Madara(
    "YonaBar",
    "https://yonabar.xyz",
    "ar",
    SimpleDateFormat("MMM dd, yyyy", Locale("ar")),
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val mangaSubString = "yaoi"
}
