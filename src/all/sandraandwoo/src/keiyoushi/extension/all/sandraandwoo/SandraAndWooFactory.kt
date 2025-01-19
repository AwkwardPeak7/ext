package keiyoushi.extension.all.sandraandwoo

import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushi.extension.all.sandraandwoo.translations.SandraAndWooDE
import keiyoushi.extension.all.sandraandwoo.translations.SandraAndWooEN

class SandraAndWooFactory : SourceFactory {
    override fun createSources() = listOf(
        SandraAndWooDE(),
        SandraAndWooEN(),
    )
}
