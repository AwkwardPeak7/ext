package keiyoushi.extension.all.snowmtl

import android.os.Build
import androidx.annotation.RequiresApi
import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushi.multisrc.machinetranslations.Language

@RequiresApi(Build.VERSION_CODES.O)
class SnowmtlFactory : SourceFactory {
    override fun createSources() = languageList.map(::Snowmtl)
}

private val languageList = listOf(
    Language("en"),
    Language("es"),
    Language("id"),
    Language("it"),
    Language("pt-BR", "pt"),
)
