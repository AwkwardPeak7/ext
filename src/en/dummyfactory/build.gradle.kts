import keiyoushi.gradle.extension.dsl.mirrorUrls

plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Factory"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Factory"
        lang = "en"
        baseUrl("https://dummyfactory.example")
    }
    source {
        name = "Dummy Factory (DE)"
        lang = "de"
        baseUrl(mirrorUrls("https://dummyfactory.example", "https://de.dummyfactory.example"))
        override("siteLang", "de")
    }
    source {
        name = "Dummy Factory (FR)"
        lang = "fr"
        baseUrl("https://dummyfactory.example")
        override("siteLang", "fr")
        // Manual id override (e.g. preserving a legacy id when name/lang/versionId changed)
        id = 1234567890L
    }
}
