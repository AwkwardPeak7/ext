package eu.kanade.tachiyomi.extension.pt.irisscanlator

import keiyoushix.multisrc.mangathemesia.MangaThemesia
import java.text.SimpleDateFormat
import java.util.Locale

class IrisScanlator : MangaThemesia(
    "Iris Scanlator",
    "https://irisscanlator.com.br",
    "pt-BR",
    dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale("pt", "BR")),
)
