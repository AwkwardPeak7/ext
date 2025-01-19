package keiyoushi.extension.en.kappabeast

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia
import org.jsoup.nodes.Document

class KappaBeast : MangaThemesia(
    "Kappa Beast",
    "https://kappabeast.com",
    "en",
) {
    override val client = super.client.newBuilder()
        .rateLimit(3)
        .build()

    override val pageSelector = ".epcontent.entry-content img"

    override fun parseGenres(document: Document): List<GenreData> {
        return document.select("li:has(input[id*='genre'])").map { li ->
            GenreData(
                li.selectFirst("label")!!.text(),
                li.selectFirst("input[type=checkbox]")!!.attr("value"),
            )
        }
    }
}
