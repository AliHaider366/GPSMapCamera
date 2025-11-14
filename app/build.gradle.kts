plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.gpsmapcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.we.map.geotag.location.video.photo.gpscamera.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "2.0.4"


        applicationVariants.all {
            outputs.all {
                val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
                val versionName = versionName
                val versionCode = versionCode
                output.outputFileName = "GPS-MAP-CAMERA-VN-$versionName-VC-$versionCode.apk"
            }
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true

//            isShrinkResources = false
//            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-video:1.3.0")
    implementation("androidx.camera:camera-extensions:1.4.2")


    implementation("com.google.openlocationcode:openlocationcode:1.0.4") //get plus code
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.github.murgupluoglu:flagkit-android:1.0.5")
    implementation("com.airbnb.android:lottie:6.3.0")

    implementation("androidx.media3:media3-transformer:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")
    implementation("androidx.media3:media3-effect:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")


    //firebase + ads + billing
    implementation("com.google.firebase:firebase-config:23.0.0")
    implementation("com.google.firebase:firebase-ads:23.6.0")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-crashlytics:20.0.0")
    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")
    implementation("com.android.billingclient:billing:8.0.0")
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))

    //In app update
    implementation ("com.google.android.play:app-update:2.1.0")
    implementation ("com.google.android.play:review:2.0.2")

    // In App Review
    implementation ("com.google.android.play:review:2.0.2")

    // App Update
    implementation("com.google.android.play:app-update:2.1.0")

    // Billing
    implementation("com.android.billingclient:billing:8.1.0")


}

