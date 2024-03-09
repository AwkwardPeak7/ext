package eu.kanade.tachiyomi.extension.es.ikifeng

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushix.multisrc.madara.Madara
import okhttp3.Response
import java.text.SimpleDateFormat
import java.util.Locale

class Ikifeng : Madara("Lector Online", "https://lectorunm.life", "es", SimpleDateFormat("dd/MM/yyyy", Locale("es"))) {
    // Ikifeng (es) -> Lector Online (es)
    override val id = 2087311173049672570

    override val useNewChapterEndpoint = true

    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).reversed()
    }
}
