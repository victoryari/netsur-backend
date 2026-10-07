plugins {
    `kotlin-android`
    `kotlin-android-extensions`
    `compose.compiler`
}

android {
    namespace = "com.ticket.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ticket"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compose {
        kotlinCompilerVersion = "1.9.23"
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}

dependencies {
    implementation("compose:compose-bom:2023.12.01")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.ui:ui-graphics:1.6.7")
    implementation("androidx.compose.ui:ui-tooling:1.6.7")
    implementation("androidx.compose.material:material:1.6.7")
    implementation("androidx.compose.material:material-icons:1.6.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.android.material:material:1.12.0")
}