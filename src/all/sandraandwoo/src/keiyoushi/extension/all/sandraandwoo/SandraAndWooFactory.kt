package keiyoushi.extension.all.sandraandwoo

import keiyoushi.extension.all.sandraandwoo.translations.SandraAndWooDE
import keiyoushi.extension.all.sandraandwoo.translations.SandraAndWooEN
import eu.kanade.tachiyomi.source.SourceFactory

class SandraAndWooFactory : SourceFactory {
    override fun createSources() = listOf(
        SandraAndWooDE(),
        SandraAndWooEN(),
    )
}
