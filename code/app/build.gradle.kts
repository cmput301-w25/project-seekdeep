plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    //id("com.android.application")     //Not used in Lab 5 so not sure about it.
}

android {
    namespace = "com.example.project_seekdeep"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.project_seekdeep"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    testImplementation(libs.ext.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.android.material:material:1.12.0")
    // FirebaseUI Storage only
    implementation ("com.firebaseui:firebase-ui-storage:7.2.0")

    // Espresso UI testing library
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("com.google.android.gms:play-services-maps:18.2.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    //Dependencies for image uploading/displaying
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("androidx.activity:activity:1.7.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.firestore)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Mockito testing dependencies
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    implementation("net.bytebuddy:byte-buddy:1.17.1")

    // Maps SDK for Android
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    //PowerMock testing dependencies
    testImplementation("org.powermock:powermock-api-mockito:1.4.12")
    testImplementation("org.powermock:powermock-module-junit4:1.6.2")
}
