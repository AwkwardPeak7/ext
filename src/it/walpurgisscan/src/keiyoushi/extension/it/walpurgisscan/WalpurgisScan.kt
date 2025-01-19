package keiyoushi.extension.it.walpurgisscan

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class WalpurgisScan : MangaThemesia("Walpurgi Scan", "https://www.walpurgiscan.it", "it", dateFormat = SimpleDateFormat("MMM d, yyyy", Locale("it"))) {
    override val id = 6566957355096372149
}
