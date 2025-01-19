package keiyoushi.extension.fr.yaoiscan

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class YaoiScan : MangaThemesia(
    name = "YaoiScan",
    baseUrl = "https://yaoiscan.fr",
    lang = "fr",
    mangaUrlDirectory = "/catalogue",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.FRANCE),
) {
    override val seriesStatusSelector = ".status-value"
}
