package keiyoushi.extension.es.mangacrab

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceScreen
import keiyoushi.lib.randomua.addRandomUAPreferenceToScreen
import keiyoushi.lib.randomua.getPrefCustomUA
import keiyoushi.lib.randomua.getPrefUAType
import keiyoushi.lib.randomua.setRandomUserAgent
import keiyoushi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.ConfigurableSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.Locale

class MangaCrab :
    Madara(
        "Manga Crab",
        "https://wikicrab.xyz",
        "es",
        SimpleDateFormat("dd/MM/yyyy", Locale("es")),
    ),
    ConfigurableSource {

    private val preferences: SharedPreferences =
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)

    override val client = super.client.newBuilder()
        .setRandomUserAgent(
            preferences.getPrefUAType(),
            preferences.getPrefCustomUA(),
        )
        .rateLimit(1, 2)
        .build()

    override val mangaSubString = "series"
    override val useLoadMoreRequest = LoadMoreStrategy.Never

    override fun chapterListSelector() = "div.listing-chapters_wrap > ul > li"
    override val mangaDetailsSelectorDescription = "div.c-page__content div.modal-contenido"

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        addRandomUAPreferenceToScreen(screen)
    }
}
