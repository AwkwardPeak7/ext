package eu.kanade.tachiyomi.extension.en.toongod

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ToonGod : Madara("ToonGod", "https://www.toongod.org", "en", SimpleDateFormat("d MMM yyyy", Locale.US)) {
    override val mangaSubString = "webtoons"
    override val useNewChapterEndpoint = false
}
