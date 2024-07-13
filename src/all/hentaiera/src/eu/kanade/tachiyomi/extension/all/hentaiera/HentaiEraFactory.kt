package eu.kanade.tachiyomi.extension.all.hentaiera

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class HentaiEraFactory : SourceFactory {

    override fun createSources(): List<Source> = listOf(
        HentaiEra("en"),
        HentaiEra("ja", "jp"),
        HentaiEra("es"),
        HentaiEra("fr"),
        HentaiEra("ko", "kr"),
        HentaiEra("de"),
        HentaiEra("ru"),
        HentaiEra("all"),
    )
}
