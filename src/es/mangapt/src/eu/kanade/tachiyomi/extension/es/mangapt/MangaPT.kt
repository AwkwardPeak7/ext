package eu.kanade.tachiyomi.extension.es.mangapt

import keiyoushix.multisrc.madara.Madara
import java.text.SimpleDateFormat
import java.util.Locale

class MangaPT : Madara(
    "MangaPT",
    "https://mangapt.com",
    "es",
    SimpleDateFormat("dd/MM/yyyy", Locale("es")),
)
