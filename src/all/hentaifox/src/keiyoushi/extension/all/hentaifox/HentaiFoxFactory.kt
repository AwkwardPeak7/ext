package keiyoushi.extension.all.hentaifox

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushi.multisrc.galleryadults.GalleryAdults

class HentaiFoxFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        HentaiFox("en", GalleryAdults.LANGUAGE_ENGLISH),
        HentaiFox("ja", GalleryAdults.LANGUAGE_JAPANESE),
        HentaiFox("zh", GalleryAdults.LANGUAGE_CHINESE),
        HentaiFox("ko", GalleryAdults.LANGUAGE_KOREAN),
        HentaiFox("all", GalleryAdults.LANGUAGE_MULTI),
    )
}
