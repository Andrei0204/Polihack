plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.telefonapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.telefonapp"
        minSdk = 27
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // When using the BoM, you don't specify versions in Firebase library dependencies
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.stripe:stripe-android:20.21.2")
    implementation ("com.github.kittinunf.fuel:fuel:2.3.1")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.stripe:stripe-android:21.2.1")
    implementation("com.github.parse-community.Parse-SDK-Android:parse:4.3.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.wallet)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}