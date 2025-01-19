package keiyoushi.extension.en.kaiscans

import eu.kanade.tachiyomi.network.interceptor.rateLimit
import keiyoushi.multisrc.mangathemesia.MangaThemesia

class LuaScans : MangaThemesia("Lua Scans (unoriginal)", "https://ponvi.online", "en") {
    override val client = super.client.newBuilder()
        .rateLimit(2)
        .build()

    override val id = 4825368993215448425
}
