package eu.kanade.tachiyomi.extension.es.senpaiediciones

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class SenpaiEdiciones : MangaThemesia(
    "Senpai Ediciones",
    "https://senpaiediciones.com",
    "es",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("es")),
) {
    override val pageSelector = "div#readerarea img:not(noscript img)"
}
