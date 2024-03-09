package eu.kanade.tachiyomi.extension.ja.mangamate

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class MangaMate : MangaThemesia(
    "漫画メイト",
    "https://manga-mate.org",
    "ja",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("ja")),
) {
    override val seriesAuthorSelector = ".fmed b:contains(作者) + span"
}
