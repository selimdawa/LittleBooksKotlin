plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.flatcode.littlebooks"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.flatcode.littlebooks"
        minSdk = 24
        targetSdk = 37
        versionCode = 9
        versionName = "1.40"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    //signingConfigs {
    //    create("release") {
    //        storeFile = file("D:\\MyProjects\\Kotlin\\Little Books\\Little Books\\LittleBooks.jks")
    //        storePassword = "00000000"
    //        keyAlias = "LittleBooks"
    //        keyPassword = "00000000"
    //    }
    //}
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    //buildTypes {
    //    getByName("release") {
    //        signingConfig = signingConfigs.getByName("release")
    //        isMinifyEnabled = true
    //        isShrinkResources = true
    //        proguardFiles(
    //            getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
    //        )
    //    }
    //}
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
    //Image
    implementation(libs.circleimageview)                //Circle Image
    implementation(libs.glide)                          //Glide Image
    implementation(libs.glide.transformations)          //Glide Image Blur
    api(libs.android.image.cropper)                     //Image Crop
    implementation(libs.autoimageslider)                //Slider Show
    // Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)
    //implementation(libs.firebase.crashlytics)
    //Other's
    implementation(libs.material.ripple)                //Ripple Effect
    implementation(libs.nafisbottomnav)                 //Bottom Navigation
    implementation(libs.play.services.ads)              //ADs Google AdMob
    implementation(libs.android.pdf.viewer)             //PDF View
}