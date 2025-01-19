package keiyoushi.extension.tr.hayalistic

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Hayalistic : Madara(
    "Hayalistic",
    "https://hayalistic.com.tr",
    "tr",
    dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
)
