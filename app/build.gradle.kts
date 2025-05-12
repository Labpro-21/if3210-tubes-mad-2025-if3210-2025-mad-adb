plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.adbpurrytify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.adbpurrytify"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

    //        annotationProcessorOptions {
//            arguments += ["room.schemaLocation": "$projectDir/schemas".toString()]
//        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // live data
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.8")

    // for sys ui colors
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06") // Use the latest stable/alpha version

    // "Diajarkan di kelas",
    // meanwhile ppt yg di up di edunex belom ada apa2 tentang Android SDK </3
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0")) // BOM for consistent versions
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")


    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.8")



    implementation("androidx.media3:media3-common:1.4.1")
    implementation("io.coil-kt.coil3:coil:3.0.0")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-alpha09")
//    implementation("androidx.media3:media3-ui:1.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // Room
    implementation("androidx.room:room-ktx:2.7.0")
    implementation("androidx.room:room-runtime:2.7.0")
    ksp("androidx.room:room-compiler:2.7.0")

    // QR generator
    implementation(libs.qrgenerator)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}