package keiyoushi.extension.all.ravensscans

import eu.kanade.tachiyomi.source.SourceFactory
import keiyoushi.multisrc.readerfront.ReaderFront

class RavensScansFactory : SourceFactory {
    override fun createSources() = listOf(RavensScans("es"), RavensScans("en"))

    class RavensScans(lang: String) :
        ReaderFront("Ravens Scans", "https://ravens-scans.com", lang) {
        override fun getImageCDN(path: String, width: Int) =
            "https://i${(0..2).random()}.wp.com/img-cdn1.ravens-scans.com" +
                "$path?strip=all&quality=100&w=$width"
    }
}
