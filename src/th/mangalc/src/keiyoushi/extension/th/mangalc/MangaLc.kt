package keiyoushi.extension.th.mangalc

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaLc : Madara(
    "Manga-Lc",
    "https://manga-lc.net",
    "th",
    dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("th")),
) {
    override val pageListParseSelector = ".reading-content img"

    override val useLoadMoreRequest = LoadMoreStrategy.Never
    override val useNewChapterEndpoint = false

    override val filterNonMangaItems = false
}
