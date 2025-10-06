plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.morsecode"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.morsecode"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }


}

dependencies {

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(":openCVLib"))
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")

    val nav_version = "2.3.5"
    implementation ("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation ("androidx.navigation:navigation-ui-ktx:$nav_version")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

//    // CameraX core library using the camera2 implementation
//    val cameraxVersion = "1.4.0-alpha04"
//    // The following line is optional, as the core library is included indirectly by camera-camera2
//    implementation("androidx.camera:camera-core:${cameraxVersion}")
//    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
//    // If you want to additionally use the CameraX Lifecycle library
//    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
//    // If you want to additionally use the CameraX VideoCapture library
//    // implementation("androidx.camera:camera-video:${cameraxVersion}")
//    // If you want to additionally use the CameraX View class
//    implementation("androidx.camera:camera-view:${cameraxVersion}")
//    // If you want to additionally add CameraX ML Kit Vision Integration
//    implementation("androidx.camera:camera-mlkit-vision:${cameraxVersion}")
//    // If you want to additionally use the CameraX Extensions library
//    // implementation("androidx.camera:camera-extensions:${cameraxVersion}")

    // CameraX core library
    val camerax_version = "1.1.0-beta03"
    implementation ("androidx.camera:camera-core:$camerax_version")

    // CameraX Camera2 extensions
    implementation ("androidx.camera:camera-camera2:$camerax_version")

    // CameraX Lifecycle library
    implementation ("androidx.camera:camera-lifecycle:$camerax_version")

    // CameraX View class
    implementation ("androidx.camera:camera-view:$camerax_version")

    //WindowManager
    implementation ("androidx.window:window:1.0.0-alpha09")

    //implementation ("org.tensorflow:tensorflow-lite-task-vision:0.4.0")
    // Import the GPU delegate plugin Library for GPU inference
    //implementation ("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.0")
    //implementation ("org.tensorflow:tensorflow-lite-gpu:2.9.0")
}