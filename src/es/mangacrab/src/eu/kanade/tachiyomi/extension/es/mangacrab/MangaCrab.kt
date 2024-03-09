package eu.kanade.tachiyomi.extension.es.mangacrab

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.interceptor.rateLimit
import eu.kanade.tachiyomi.source.ConfigurableSource
import keiyoushix.lib.randomua.addRandomUAPreferenceToScreen
import keiyoushix.lib.randomua.getPrefCustomUA
import keiyoushix.lib.randomua.getPrefUAType
import keiyoushix.lib.randomua.setRandomUserAgent
import keiyoushix.multisrc.madara.Madara
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.text.SimpleDateFormat
import java.util.Locale

class MangaCrab :
    Madara(
        "Manga Crab",
        "https://httpmangacrab2.com",
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

    override fun chapterListSelector() = "div.listing-chapters_wrap > ul > li"
    override val mangaDetailsSelectorDescription = "div.c-page__content div.modal-contenido"

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        addRandomUAPreferenceToScreen(screen)
    }
}
