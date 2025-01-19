package keiyoushi.extension.en.mysticalmerries

import eu.kanade.tachiyomi.network.GET
import keiyoushi.multisrc.madara.Madara

class MysticalMerries : Madara("Mystical Merries", "https://mysticalmerries.com", "en") {
    override fun popularMangaRequest(page: Int) = GET("$baseUrl/genre/manhwa/page/$page/?m_orderby=trending", headers)
    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/genre/manhwa/page/$page/?m_orderby=latest", headers)
    override fun popularMangaNextPageSelector(): String? = "div.nav-previous"
    override fun latestUpdatesNextPageSelector(): String? = popularMangaNextPageSelector()
}
