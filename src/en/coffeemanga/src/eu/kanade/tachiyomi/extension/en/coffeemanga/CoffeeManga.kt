package eu.kanade.tachiyomi.extension.en.coffeemanga

import eu.kanade.tachiyomi.multisrc.madara.ChapterFetchMethod
import eu.kanade.tachiyomi.multisrc.madara.Madara

class CoffeeManga : Madara("Coffee Manga", "https://coffeemanga.ink", "en") {
    override val chapterFetchMethod = ChapterFetchMethod.AJAX_V2
}
