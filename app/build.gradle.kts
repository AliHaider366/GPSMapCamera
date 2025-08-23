plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.gpsmapcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gpsmapcamera"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding=true
        buildConfig=true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation ("com.intuit.sdp:sdp-android:1.1.1")

    implementation ("jp.co.cyberagent.android:gpuimage:2.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.google.mlkit:barcode-scanning:17.3.0")

    implementation ("androidx.camera:camera-core:1.4.2")
    implementation ("androidx.camera:camera-camera2:1.4.2")
    implementation ("androidx.camera:camera-lifecycle:1.4.2")
    implementation ("androidx.camera:camera-view:1.4.2")
    implementation("androidx.camera:camera-video:1.3.0")
    implementation ("androidx.camera:camera-extensions:1.4.2")

    implementation("com.github.Dimezis:BlurView:version-3.1.0")

    implementation("com.google.openlocationcode:openlocationcode:1.0.4") //get plus code

}

