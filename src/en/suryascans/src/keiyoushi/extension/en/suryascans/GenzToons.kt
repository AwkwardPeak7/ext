package keiyoushi.extension.en.suryascans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.keyoapp.Keyoapp
import java.util.concurrent.TimeUnit

class GenzToons : Keyoapp(
    "Genz Toons",
    "https://genzupdates.com",
    "en",
) {

    override val client = super.client.newBuilder()
        .rateLimit(3)
        .connectTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()
}
