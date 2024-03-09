package eu.kanade.tachiyomi.extension.en.pmscans

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class PMScans : Madara(
    "PMScans",
    "https://rackusreads.com",
    "en",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val versionId = 2
    override val useNewChapterEndpoint = true
}
