plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.flatcode.littlebooksadmin"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.flatcode.littlebooksadmin"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "1.30"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.preference.ktx)           //Shared Preference
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Layout
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    // Image
    implementation(libs.circleimageview)                //Circle Image
    implementation(libs.glide)                          //Glide Image
    api(libs.android.image.cropper)                     //Image Crop
    //implementation(libs.glide.transformations)          //Image Blur
    //Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    //implementation(libs.firebase.crashlytics)
    //Other's
    implementation(libs.material.ripple)                //Ripple Effect
    implementation(libs.android.pdf.viewer)             //PDF View
}