package keiyoushi.extension.id.worldmanhwas

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class WorldManhwas : Madara("WorldManhwas", "https://worldmanhwas.zone", "id", SimpleDateFormat("MMMM dd, yyyy", Locale("id"))) {
    override val useNewChapterEndpoint = false
}
