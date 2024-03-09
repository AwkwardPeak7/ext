package eu.kanade.tachiyomi.extension.fr.scanhentaifr

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ScanHentaiFR : Madara("Scan Hentai FR", "https://scan-hentai.fr", "fr", dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH))
