import keiyoushi.gradle.extension.dsl.mirrorUrls

plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Configurable"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Configurable"
        lang = "en"
        baseUrl(
            mirrorUrls(
                "https://dummyconfigurable.example",
                "https://alt.dummyconfigurable.example",
            ),
        )
    }
}
