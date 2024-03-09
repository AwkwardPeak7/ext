package eu.kanade.tachiyomi.extension.en.mangakitsune

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushix.multisrc.madara.Madara
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale

class MangaKitsune : Madara("MangaKitsune", "https://mangakitsune.com", "en", dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)) {

    override fun chapterListParse(response: Response): List<SChapter> = super.chapterListParse(response).reversed()
}
