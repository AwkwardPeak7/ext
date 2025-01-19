package keiyoushi.extension.id.siimanga

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Page
import okhttp3.Request

class Siikomik : MangaThemesia(
    "Siikomik",
    "https://siikomik.cc",
    "id",
) {
    override val versionId = 2

    override val hasProjectPage = true

    override fun imageRequest(page: Page): Request {
        val imageHeaders = headers.newBuilder()
            .removeAll("Referer")
            .build()
        return GET(page.imageUrl!!, imageHeaders)
    }
}
