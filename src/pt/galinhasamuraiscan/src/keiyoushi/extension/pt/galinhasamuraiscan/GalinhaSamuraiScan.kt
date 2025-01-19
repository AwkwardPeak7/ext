package keiyoushi.extension.pt.galinhasamuraiscan

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class GalinhaSamuraiScan : Madara(
    "Galinha Samurai Scan",
    "https://galinhasamurai.com",
    "pt-BR",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {
    override val useLoadMoreRequest = LoadMoreStrategy.Always
    override val useNewChapterEndpoint = true
}
