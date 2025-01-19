package keiyoushi.extension.pt.hianatoscan

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class HianatoScan : Madara(
    "Hianato Scan",
    "https://hianatoscan.top",
    "pt-BR",
    SimpleDateFormat("MMMM dd, yyyy", Locale("pt", "BR")),
) {
    override val useNewChapterEndpoint = true
}
