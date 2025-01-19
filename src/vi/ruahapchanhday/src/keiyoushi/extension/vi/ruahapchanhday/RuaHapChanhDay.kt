package keiyoushi.extension.vi.ruahapchanhday

import keiyoushi.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class RuaHapChanhDay : Madara(
    "Rua Hap Chanh Day",
    "https://ruahapchanhday.com",
    "vi",
    SimpleDateFormat("dd/MM/yyyy", Locale.ROOT),
) {
    override val useNewChapterEndpoint = true

    override val useLoadMoreRequest = LoadMoreStrategy.Always
}
