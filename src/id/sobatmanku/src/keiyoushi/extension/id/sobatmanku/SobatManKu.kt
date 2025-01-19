package keiyoushi.extension.id.sobatmanku

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushi.multisrc.zeistmanga.ZeistManga
import okhttp3.Response

class SobatManKu : ZeistManga("SobatManKu", "https://www.sobatmanku19.cab", "id") {

    override val hasFilters = true

    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).onEach {
            // fix some chapter name
            it.name = it.name.run {
                substring(indexOf("Chapter"))
            }
        }
    }
}
