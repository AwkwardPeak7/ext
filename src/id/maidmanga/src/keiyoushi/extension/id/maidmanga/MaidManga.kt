package keiyoushi.extension.id.maidmanga

import keiyoushi.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class MaidManga : ZManga("Maid - Manga", "https://www.maid.my.id", "id", SimpleDateFormat("MMM d, yyyy", Locale("id"))) {
    override val hasProjectPage = true
}
