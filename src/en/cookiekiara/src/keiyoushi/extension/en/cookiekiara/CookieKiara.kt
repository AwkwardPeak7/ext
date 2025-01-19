package keiyoushi.extension.en.cookiekiara

import eu.kanade.tachiyomi.source.model.SChapter
import keiyoushi.multisrc.madara.Madara
import okhttp3.Response

class CookieKiara : Madara("Cookie Kiara", "https://18.kiara.cool", "en") {
    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).reversed()
    }
}
