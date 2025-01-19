package keiyoushi.extension.ru.senkuro

import androidx.preference.PreferenceScreen
import keiyoushi.multisrc.senkuro.Senkuro

class Senkuro : Senkuro("Senkuro", "https://senkuro.com", "ru") {
    override fun setupPreferenceScreen(screen: PreferenceScreen) {}
}
