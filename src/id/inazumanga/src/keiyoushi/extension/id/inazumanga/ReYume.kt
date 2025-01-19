package keiyoushi.extension.id.inazumanga

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.util.asJsoup
import keiyoushi.multisrc.makaru.Makaru
import okhttp3.Response

class ReYume : Makaru("ReYume", "https://www.re-yume.my.id", "id") {
    override fun pageListParse(response: Response): List<Page> {
        val document = response.asJsoup()

        return document.select(".post-body img").mapIndexed { idx, img ->
            Page(idx, imageUrl = img.absUrl("src"))
        }
    }
}
