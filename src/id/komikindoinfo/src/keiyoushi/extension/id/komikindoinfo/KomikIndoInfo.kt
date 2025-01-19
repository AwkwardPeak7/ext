package keiyoushi.extension.id.komikindoinfo

import keiyoushi.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class KomikIndoInfo : ZManga("KomikIndo.info", "https://komikindo.info", "id", dateFormat = SimpleDateFormat("MMM d, yyyy", Locale("id"))) {

    override val hasProjectPage = true
}
