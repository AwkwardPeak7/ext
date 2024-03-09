package eu.kanade.tachiyomi.extension.fr.raijinscans

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RaijinScans : Madara("Raijin Scans", "https://raijinscans.fr", "fr", dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH))
