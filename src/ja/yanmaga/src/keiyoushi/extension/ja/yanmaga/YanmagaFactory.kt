package keiyoushi.extension.ja.yanmaga

import eu.kanade.tachiyomi.source.SourceFactory

class YanmagaFactory : SourceFactory {
    override fun createSources() = listOf(
        YanmagaComics(),
        YanmagaGravures(),
    )
}
