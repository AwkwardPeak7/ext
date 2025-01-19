package keiyoushi.extension.tr.anikiga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class Anikiga : Madara("Anikiga", "https://anikiga.com", "tr", SimpleDateFormat("d MMMMM yyyy", Locale("tr")))
