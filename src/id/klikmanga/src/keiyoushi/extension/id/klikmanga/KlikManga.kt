package keiyoushi.extension.id.klikmanga

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class KlikManga : Madara(
    "KlikManga",
    "https://klikmanga.com",
    "id",
    SimpleDateFormat("MMMM dd, yyyy", Locale("id")),
) {
    override val mangaSubString = "daftar-komik"

    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
