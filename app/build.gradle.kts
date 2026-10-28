plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.skinlytic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skinlytic"
        minSdk = 26
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }
}

val camerax_version = "1.2.3"

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // TensorFlow Lite (Flex ops / Select TF Ops)
    implementation("org.tensorflow:tensorflow-lite:2.13.0")                // core TFLite
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.13.0")  // Flex ops
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")         // support ops helper
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.3")        // optional

    // Glide untuk loading image
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // CameraX
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")
}
