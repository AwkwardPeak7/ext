package keiyoushi.extension.id.komikgan

import keiyoushi.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class KomikGan : ZManga("KomikGan", "https://komikgan.com", "id", SimpleDateFormat("MMM d, yyyy", Locale("id"))) {

    override val hasProjectPage = true
}
