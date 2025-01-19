package keiyoushi.extension.it.tuttoanimemanga

import keiyoushi.multisrc.pizzareader.PizzaReader
import kotlinx.serialization.json.Json

class TuttoAnimeManga : PizzaReader("TuttoAnimeManga", "https://tuttoanimemanga.net", "it") {
    override val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}
