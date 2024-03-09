package eu.kanade.tachiyomi.extension.es.midnightmanga

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MidnightManga : Madara("MidnightManga", "http://midnightmanga.com", "es", dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale("es")))
