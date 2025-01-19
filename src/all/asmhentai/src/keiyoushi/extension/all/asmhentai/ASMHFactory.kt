package keiyoushi.extension.all.asmhentai

import keiyoushi.multisrc.galleryadults.GalleryAdults
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class ASMHFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        AsmHentai("en", GalleryAdults.LANGUAGE_ENGLISH),
        AsmHentai("ja", GalleryAdults.LANGUAGE_JAPANESE),
        AsmHentai("zh", GalleryAdults.LANGUAGE_CHINESE),
        AsmHentai("all", GalleryAdults.LANGUAGE_MULTI),
    )
}
