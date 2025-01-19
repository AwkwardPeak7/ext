package keiyoushi.extension.fr.hentaizone

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class HentaiZone : Madara("HentaiZone", "https://hentaizone.xyz", "fr", dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.FRENCH)) {
    override val mangaSubString = "tous-les-mangas"
}
