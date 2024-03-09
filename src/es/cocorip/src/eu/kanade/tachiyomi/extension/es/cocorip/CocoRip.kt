package eu.kanade.tachiyomi.extension.es.cocorip

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class CocoRip : Madara("Coco Rip", "https://cocorip.net", "es", SimpleDateFormat("dd/MM/yyyy", Locale("es"))) {
    override val mangaDetailsSelectorDescription = "div.summary__content"
}
