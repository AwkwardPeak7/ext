package keiyoushi.extension.all.asmhentai

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushi.multisrc.galleryadults.GalleryAdults

class ASMHFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        AsmHentai("en", GalleryAdults.LANGUAGE_ENGLISH),
        AsmHentai("ja", GalleryAdults.LANGUAGE_JAPANESE),
        AsmHentai("zh", GalleryAdults.LANGUAGE_CHINESE),
        AsmHentai("all", GalleryAdults.LANGUAGE_MULTI),
    )
}
