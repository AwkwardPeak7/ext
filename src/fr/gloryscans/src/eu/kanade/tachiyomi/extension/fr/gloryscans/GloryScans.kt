package eu.kanade.tachiyomi.extension.fr.gloryscans

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class GloryScans : MangaThemesia("Glory Scans", "https://gloryscans.fr", "fr", dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.FRENCH))
