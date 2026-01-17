plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.flatcode.littlebooksadmin"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.flatcode.littlebooksadmin"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.25"

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
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
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
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.cardview)
    //Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    //implementation(libs.firebase.crashlytics)
    // Image
    implementation(libs.circleimageview)                //Circle Image
    implementation(libs.glide)                          //Glide Image
    // noinspection KaptUsageInsteadOfKsp
    kapt(libs.compiler)                                 //Glide Compiler
    implementation(libs.material.ripple)                //Ripple Effect
    api(libs.android.image.cropper)                     //Image Crop
    //implementation(libs.glide.transformations)          //Image Blur

    //PDF
    //noinspection GradleDependency
    implementation(libs.android.pdf.viewer)      //PDF View
}