package keiyoushi.extension.id.futari

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Futari : MangaThemesia(
    "Futari",
    "https://futari.info",
    "id",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("id")),
) {
    override val hasProjectPage = true
}
