plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "demo.idv.shao.heartratemonitor"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.1")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.recyclerview:recyclerview:1.1.0-alpha04")
    implementation("com.polidea.rxandroidble2:rxandroidble:1.9.1")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.airbnb.android:lottie:3.0.1")
    implementation("com.google.android.material:material:1.1.0-alpha06")

    val navVersion = "2.1.0-alpha02"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    val room_version = "2.1.0-alpha07"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version") // For Kotlin use kapt instead of annotationProcessor
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-rxjava2:$room_version")

    implementation("com.google.code.gson:gson:2.8.5")

    testImplementation("junit:junit:4.12")
    testImplementation("androidx.room:room-testing:$room_version")
    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1")
}

