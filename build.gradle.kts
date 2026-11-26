plugins {
    alias(libs.plugins.android.application) apply false
    // Not applied (AGP 9 has built-in Kotlin). Declared only to pin the Kotlin version,
    // which must match the Compose compiler plugin.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
