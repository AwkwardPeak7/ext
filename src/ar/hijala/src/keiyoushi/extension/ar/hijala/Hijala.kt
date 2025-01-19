package keiyoushi.extension.ar.hijala

import keiyoushi.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class Hijala : MangaThemesia(
    "Hijala",
    "https://hijala.com",
    "ar",
    dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale("ar")),
) {
    // Site moved from ZeistManga to MangaThemesia again
    override val versionId get() = 2
}
