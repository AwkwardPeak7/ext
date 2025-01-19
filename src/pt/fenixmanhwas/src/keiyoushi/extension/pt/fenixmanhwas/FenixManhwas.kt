package keiyoushi.extension.pt.fenixmanhwas
import keiyoushi.multisrc.madara.Madara

class FenixManhwas : Madara(
    "Fênix Manhwas",
    "https://fenixscan.xyz",
    "pt-BR",
) {
    override val versionId = 2
    override val useNewChapterEndpoint = true
}
