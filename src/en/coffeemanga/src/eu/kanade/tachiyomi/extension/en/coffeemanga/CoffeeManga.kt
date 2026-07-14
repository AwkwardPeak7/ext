package eu.kanade.tachiyomi.extension.en.coffeemanga

import eu.kanade.tachiyomi.multisrc.madara.Madara
import keiyoushi.annotation.Source

@Source
abstract class CoffeeManga : Madara() {
    // override val chapterFetchMethod = ChapterFetchMethod.AJAX_V2
}
