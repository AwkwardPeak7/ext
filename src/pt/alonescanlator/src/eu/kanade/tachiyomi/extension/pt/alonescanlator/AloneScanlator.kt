package eu.kanade.tachiyomi.extension.pt.alonescanlator

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class AloneScanlator : Madara(
    "Alone Scanlator",
    "https://alonescanlator.com.br",
    "pt-BR",
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")),
) {
    override val useNewChapterEndpoint: Boolean = true
}
