package eu.kanade.tachiyomi.extension.ru.senkuro

import androidx.preference.PreferenceScreen
import keiyoushix.multisrc.senkuro.Senkuro

class Senkuro : Senkuro("Senkuro", "https://senkuro.com", "ru") {
    override fun setupPreferenceScreen(screen: PreferenceScreen) {}
}
