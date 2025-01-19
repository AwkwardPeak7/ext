package keiyoushi.extension.it.hastateam

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.pizzareader.PizzaReader
import kotlinx.serialization.json.Json

class HastaTeam : PizzaReader(
    "Hasta Team",
    "https://reader.hastateam.com",
    "it",
) {
    override val client = super.client.newBuilder()
        .addInterceptor { chain ->
            val url = chain.request().url.newBuilder()
                .scheme("https")
                .build()

            val request = chain.request().newBuilder()
                .url(url)
                .build()

            chain.proceed(request)
        }
        .rateLimit(1)
        .build()

    override val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
