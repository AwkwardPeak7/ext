package eu.kanade.tachiyomi.extension.fr.readergen

import keiyoushix.multisrc.madara.Madara

class ReaderGen : Madara("ReaderGen", "https://fr.readergen.fr", "fr") {
    override val useNewChapterEndpoint = true
}
