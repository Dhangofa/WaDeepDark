plugins {
    id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.dhangofa.wadeepdark"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dhangofa.wadeepdark"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
	kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
