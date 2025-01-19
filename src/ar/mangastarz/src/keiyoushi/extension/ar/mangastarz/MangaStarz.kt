package keiyoushi.extension.ar.mangastarz

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaStarz : Madara(
    "Manga Starz",
    "https://manga-starz.com",
    "ar",
    dateFormat = SimpleDateFormat("d MMMM، yyyy", Locale("ar")),
) {
    override val chapterUrlSuffix = ""
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = false
}
