package keiyoushi.extension.id.manhwaindo

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ManhwaIndo : MangaThemesia(
    "Manhwa Indo",
    "https://manhwaindo.one",
    "id",
    "/series",
    SimpleDateFormat("MMMM dd, yyyy", Locale.US),
) {
    override val hasProjectPage = true

    override fun pageListParse(response: Response) =
        super.pageListParse(response).distinctBy {
            it.imageUrl!!
        }
}
