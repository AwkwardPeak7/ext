import keiyoushi.gradle.extension.dsl.mirrorUrls

plugins { alias(kei.plugins.extension) }

extension {
    name = "Dummy Mirrors"
    versionCode = 1
    nsfw = false
    source {
        name = "Dummy Mirrors"
        lang = "en"
        baseUrl(
            mirrorUrls(
                "https://dummymirrors.example",
                "https://alt.dummymirrors.example",
                "https://backup.dummymirrors.example",
            ),
        )
    }
}
