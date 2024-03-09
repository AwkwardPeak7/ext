package eu.kanade.tachiyomi.extension.id.komikindoinfo

import keiyoushix.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class KomikIndoInfo : ZManga("KomikIndo.info", "http://komikindo.info", "id", dateFormat = SimpleDateFormat("MMM d, yyyy", Locale("id"))) {

    override val hasProjectPage = true
}
