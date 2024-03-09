package eu.kanade.tachiyomi.extension.en.drakescans

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class DrakeScans : Madara(
    "Drake Scans",
    "https://drakescans.com",
    "en",
    SimpleDateFormat("dd/MM/yyyy", Locale.US),
) {

    override val mangaDetailsSelectorTag = ""

    override val mangaSubString = "series"
}
