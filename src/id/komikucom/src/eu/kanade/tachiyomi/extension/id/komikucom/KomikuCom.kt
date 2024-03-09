package eu.kanade.tachiyomi.extension.id.komikucom

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class KomikuCom : MangaThemesia("Komiku.com", "https://komiku.com", "id", dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id")))
