plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidx.navigation.safeargs)
}

android {
    namespace = "com.bodakesatish.sandhyasbeautyservices"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bodakesatish.sandhyasbeautyservices"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "com.bodakesatish.sandhyasbeautyservices.CustomRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        //compose = true
        aidl = false
        buildConfig = false
        renderScript = false
        shaders = false
        viewBinding = true
    }

}

dependencies {

    implementation(project(":compose"))
    implementation(project(":domain"))
    implementation(project(":data"))

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Core testing

    // Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Use the latest version

    // Mockito for mocking
    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation(libs.mockito.kotlin)

    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.56.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Turbine for Flow testing
    testImplementation("app.cash.turbine:turbine:1.1.0") // Use the latest version

    androidTestImplementation("app.cash.turbine:turbine:1.1.0") // Use the latest version

    testImplementation("androidx.arch.core:core-testing:2.2.0") // For InstantTaskExecutorRule

    androidTestImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation(libs.mockito.kotlin)

    // Hilt testing (optional for these specific ViewModel tests if you instantiate directly, but good for integration tests)
    // testImplementation "com.google.dagger:hilt-android-testing:2.51"
    // kaptTest "com.google.dagger:hilt-android-compiler:2.51"

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    androidTestImplementation("androidx.arch.core:core-testing:2.2.0") // For InstantTaskExecutorRule
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}