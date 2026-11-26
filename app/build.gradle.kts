plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing comes from environment variables set by scripts/release.sh.
// The keystore itself is never committed.
val keystoreFile: String? = System.getenv("TOOLBOX_KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("TOOLBOX_KEYSTORE_PASSWORD")

// versionCode must only ever go up, or installed apps refuse the update.
// scripts/release.sh passes the last published versionCode + 1; local builds default to 1.
val appVersionCode: Int = System.getenv("TOOLBOX_VERSION_CODE")?.toInt() ?: 1

android {
    namespace = "com.theopsking.toolbox"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        // Permanent: changing applicationId makes Android treat it as a different app.
        applicationId = "com.theopsking.toolbox"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = "1.0.$appVersionCode"

        buildConfigField(
            "String",
            "UPDATE_MANIFEST_URL",
            "\"https://raw.githubusercontent.com/The-Ops-King/app-test/releases/toolbox/update.json\""
        )
    }

    signingConfigs {
        if (keystoreFile != null && keystorePassword != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                keyAlias = "toolbox"
                keyPassword = keystorePassword
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.coroutines.android)
    debugImplementation(libs.compose.ui.tooling)

    // Android's org.json is a stub in local unit tests; this supplies the real one.
    testImplementation(libs.junit)
    testImplementation(libs.org.json)
}
