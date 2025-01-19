package keiyoushi.extension.id.crotpedia

import keiyoushi.multisrc.zmanga.ZManga
import java.text.SimpleDateFormat
import java.util.Locale

class CrotPedia : ZManga("CrotPedia", "https://crotpedia.net", "id", dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("id"))) {

    override val hasProjectPage = false
}
