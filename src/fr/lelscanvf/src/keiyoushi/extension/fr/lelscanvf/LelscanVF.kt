package keiyoushi.extension.fr.lelscanvf

import keiyoushi.multisrc.fuzzydoodle.FuzzyDoodle

class LelscanVF : FuzzyDoodle("Lelscan-VF", "https://lelscanfr.com", "fr") {

    // mmrcms -> FuzzyDoodle
    override val versionId = 2

    override val latestFromHomePage = true
}
