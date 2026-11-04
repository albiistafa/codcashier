// Runs the app on a device to record which code startup and the cashier screen use,
// so it ships precompiled (the same baseline-profile setup Gojek's APK carries).
// Regenerate with: ./gradlew :app:generateBaselineProfile (needs a running emulator or phone)
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "dev.codcow.kasirku.baselineprofile"
    compileSdk = 35

    defaultConfig {
        minSdk = 31
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
