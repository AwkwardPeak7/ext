package eu.kanade.tachiyomi.extension.en.toonmany

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ToonMany : Madara("ToonMany", "https://toonmany.com", "en", dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US))
