package keiyoushi.extension.fr.poseidonscans

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class PoseidonScans : Madara(
    "PoseidonScans",
    "https://poseidonscans.fr",
    "fr",
    dateFormat = SimpleDateFormat("dd/mm/yyyy", Locale.FRANCE),
) {
    override val useNewChapterEndpoint = true
}
