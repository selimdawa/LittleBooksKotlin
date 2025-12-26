plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.flatcode.littlebooks"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.flatcode.littlebooks"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.35"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\MyProjects\\Kotlin\\Little Books\\Little Books\\LittleBooks.jks")
            storePassword = "00000000"
            keyAlias = "LittleBooks"
            keyPassword = "00000000"
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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
    implementation(libs.autoimageslider)                //Slider Show
    implementation(libs.glide.transformations)          //Image Blur
    //Bottom Navigation
    implementation(libs.nafisbottomnav)                 //Nafis Bottom Navigation

    //Ads
    implementation(libs.play.services.ads)   //ADs Google AdMob
    //PDF
    //noinspection GradleDependency
    implementation(libs.android.pdf.viewer)      //PDF View
}