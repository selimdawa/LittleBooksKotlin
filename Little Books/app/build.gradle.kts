plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.kotlinParcelize)
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

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    //Layout
    implementation(libs.material)
    implementation(libs.multicolors)
    //Image
    implementation(libs.coil)                           //Coil Image
    api(libs.android.image.cropper)                     //Image Crop
    implementation(libs.autoimageslider)                //Slider Show
    //Firebase
    implementation(platform(libs.firebase.bom)) //Firebase BOM
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    //Cloudinary
    implementation(libs.cloudinary.android)
    //MVVM
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    //Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    //Other
    implementation(libs.material.ripple)                //Ripple Effect
    implementation(libs.nafisbottomnav)                 //Bottom Navigation
    implementation(libs.play.services.ads)              //ADs Google AdMob
    implementation(libs.android.pdf.viewer)             //PDF View
    implementation(libs.timber)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}