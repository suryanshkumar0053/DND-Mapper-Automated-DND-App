plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.example.dndapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dndapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.txt"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

}
dependencies {
    // Lifecycle components for ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-service:2.6.0") // Add the appropriate version

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Retrofit for network communication
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Jetpack Compose dependencies (Using BOM for version consistency)
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.material3:material3")

    // Permissions Handling with Accompanist (Compose-friendly library)
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha") // Updated version

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:2.11.4")

    // Location Services for getting location data
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1") // For background work and notifications
    implementation("androidx.vectordrawable:vectordrawable:1.2.0")



    // Room Database (For storing location data)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore Preferences
    implementation ("androidx.datastore:datastore-preferences:1.0.0")


    // Kotlin Coroutines for background tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Updated version

    // Additional dependencies related to AndroidX libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Navigation Compose for screen navigation handling
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Coroutines test
    testImplementation("androidx.arch.core:core-testing:2.1.0") // ViewModel & LiveData testing
    testImplementation("com.google.truth:truth:1.1.3") // Fluent assertions
    testImplementation("org.robolectric:robolectric:4.11.1") // Runs tests without emulator
    testImplementation("net.bytebuddy:byte-buddy:1.14.7")
    testImplementation("io.mockk:mockk:1.13.5")
    implementation("org.slf4j:slf4j-api:2.0.12")  // Latest SLF4J API
    testImplementation("org.slf4j:slf4j-simple:2.0.12")  // Simple logging backend for tests
    testImplementation("net.bytebuddy:byte-buddy:1.14.8") // Latest version
    testImplementation("net.bytebuddy:byte-buddy-agent:1.14.8")

    // Android UI testing dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("org.mockito:mockito-android:5.5.0") // Ensure latest Mockito core
    androidTestImplementation ("org.mockito.kotlin:mockito-kotlin:5.2.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:core-ktx:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")
    // For async UI tests
    androidTestImplementation("androidx.test:orchestrator:1.4.2") // Isolates UI tests

    // Android Jetpack Compose testing
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00")) // BOM for Compose in Android tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.activity:activity-ktx:1.9.3")
}

tasks.withType<Test> {
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}



