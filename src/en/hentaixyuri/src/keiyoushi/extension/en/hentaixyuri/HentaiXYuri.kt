package keiyoushi.extension.en.hentaixyuri

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class HentaiXYuri : Madara(
    "HentaiXYuri",
    "https://hentaixyuri.com",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US),
)
