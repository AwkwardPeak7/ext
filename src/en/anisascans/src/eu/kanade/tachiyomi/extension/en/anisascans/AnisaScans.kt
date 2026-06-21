package eu.kanade.tachiyomi.extension.en.anisascans

import eu.kanade.tachiyomi.multisrc.madara.ChapterFetchMethod
import eu.kanade.tachiyomi.multisrc.madara.Madara

class AnisaScans :
    Madara(
        "Anisa Scans",
        "https://anisascans.in",
        "en",
    ) {
    override val chapterFetchMethod = ChapterFetchMethod.AJAX_V2
}
