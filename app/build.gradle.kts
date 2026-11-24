plugins {
    id("shelfie.android.application")
    id("shelfie.android.hilt")
}

android {
    namespace = "com.shelfie.app"

    defaultConfig {
        applicationId = "com.shelfie.app"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:books"))
    implementation(project(":feature:readinglist"))
    implementation(project(":feature:stats"))

    // Hilt builds the whole graph here, so every module that contributes a binding
    // (a Hilt module or an @Inject constructor) must be on this classpath.
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":core:domain"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Activity + Navigation 3
    implementation(libs.androidx.activity.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    // Timber
    implementation(libs.timber)

    // AndroidX
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
