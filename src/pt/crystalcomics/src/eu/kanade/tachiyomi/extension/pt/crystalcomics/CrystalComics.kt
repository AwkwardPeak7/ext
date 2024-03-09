package eu.kanade.tachiyomi.extension.pt.crystalcomics

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class CrystalComics : Madara(
    "Crystal Comics",
    "https://crystalcomics.com",
    "pt-BR",
    SimpleDateFormat("MMMM dd, yyyy", Locale("pt", "BR")),
) {
    override val useNewChapterEndpoint = true
}
