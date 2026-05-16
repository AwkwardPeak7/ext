import keiyoushi.gradle.extension.dsl.customBaseUrl

plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Custom URL"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Custom URL"
        lang = "en"
        baseUrl(customBaseUrl("https://dummycustom.example"))
    }
}
