package keiyoushi.extension.ar.gmangasite

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class GmangaSite : Madara(
    "GMANGA (unoriginal)",
    "https://gmanga.site",
    "ar",
    dateFormat = SimpleDateFormat("MMMM dd، yyyy", Locale("ar")),
) {
    override val chapterUrlSuffix = ""
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true
}
