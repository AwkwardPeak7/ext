import io.github.keiyoushi.gradle.api.ContentWarning

plugins {
    alias(kei.plugins.extension)
}

keiyoushi {
    name = "Kavita"
    versionCode = 6
    contentWarning = ContentWarning.SAFE
    libVersion = "1.6"

    source {
        name = "Kavita (1)"
        lang = "all"
        id = 7266875667263635239L
        baseUrl = "http://127.0.0.1:5000"
    }
    source {
        name = "Kavita (2)"
        lang = "all"
        id = 3764816291200082561L
        baseUrl = "http://127.0.0.1:5000"
    }
    source {
        name = "Kavita (3)"
        lang = "all"
        id = 778637366235559582L
        baseUrl = "http://127.0.0.1:5000"
    }

    deeplink {
        path("/library/..*")
        path("/Series/..*")
    }
}
