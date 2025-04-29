import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.entriqs"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.entriqs"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    aaptOptions.noCompress.add("tflite")
}

tasks.register("downloadMobileFaceNetModel") {
    doLast {
        val assetsDir = file("${projectDir}/src/main/assets")
        if (!assetsDir.exists()) {
            assetsDir.mkdirs()
        }

        val modelFile = file("$assetsDir/mobileFaceNet.tflite")
        if (!modelFile.exists()) {
            try {
                println("Downloading mobileFaceNet.tflite...")
                val url = "https://raw.githubusercontent.com/MCarlomagno/FaceRecognitionAuth/master/assets/mobilefacenet.tflite"
                URI(url).toURL().openStream().use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("Downloaded mobileFaceNet.tflite to ${modelFile.path}")
            } catch (e: Exception) {
                println("Failed to download mobileFaceNet.tflite: ${e.message}")
                throw GradleException("Model download failed", e)
            }
        } else {
            println("mobileFaceNet.tflite already exists at ${modelFile.path}")
        }
    }
}

tasks.named("preBuild") {
    dependsOn("downloadMobileFaceNetModel")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.gson)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.circleimageview)

    implementation(libs.tensorflow.lite) {
        exclude(group = "com.google.ai.edge.litert")
    }
    implementation(libs.tensorflow.lite.support) {
        exclude(group = "com.google.ai.edge.litert")
    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

configurations.all {
    resolutionStrategy {
        force("org.tensorflow:tensorflow-lite:2.16.1")
        force("org.tensorflow:tensorflow-lite-support:0.4.4")
    }
}