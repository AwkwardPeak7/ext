package keiyoushi.extension.es.senpaiediciones

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class SenpaiEdiciones : MangaThemesia(
    "Senpai Ediciones",
    "https://senpaimangas.online",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val pageSelector = "div#readerarea img:not(noscript img)"
}
