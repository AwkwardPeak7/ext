import keiyoushi.gradle.extensions.baseVersionCode
import keiyoushi.gradle.extensions.libVersion

plugins {
    alias(kei.plugins.multisrc)
}

baseVersionCode = 51
libVersion = "1.6"

dependencies {
    // api(project(":lib:cryptoaes"))
    // api(project(":lib:i18n"))
    api(project(":lib:cookieinterceptor"))
}
