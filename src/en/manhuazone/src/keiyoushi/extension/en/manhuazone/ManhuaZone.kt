package keiyoushi.extension.en.manhuazone

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class ManhuaZone : Madara(
    "ManhuaZone",
    "https://manhuazone.org",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US),
)
