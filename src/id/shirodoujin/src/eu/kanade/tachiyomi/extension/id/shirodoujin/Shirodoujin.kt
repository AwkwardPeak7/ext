package eu.kanade.tachiyomi.extension.id.shirodoujin

import keiyoushix.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class Shirodoujin : ZManga("Shiro Doujin", "https://shirodoujin.com", "id", SimpleDateFormat("MMM d, yyyy", Locale("id"))) {
    override val hasProjectPage = true
}
