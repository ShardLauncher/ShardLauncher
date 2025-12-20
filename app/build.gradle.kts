import java.io.ByteArrayOutputStream
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

// --- 读取 local.properties ---
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
// --------------------------

fun gitBranch(): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = byteOut
        }
        String(byteOut.toByteArray()).trim()
    } catch (e: Exception) {
        "unknown"
    }
}

fun gitHash(): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = byteOut
        }
        String(byteOut.toByteArray()).trim()
    } catch (e: Exception) {
        "unknown"
    }
}

fun gitCommitDate(): String {
    return try {
        val byteOut = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "log", "-1", "--format=%ai")
            standardOutput = byteOut
        }
        String(byteOut.toByteArray()).trim()
    } catch (e: Exception) {
        "unknown"
    }
}

android {
    namespace = "com.lanrhyme.shardlauncher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lanrhyme.shardlauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1220
        val vName = "a0.25.1220"
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val isRelease = project.hasProperty("isReleaseBuild")
        val buildStatus = if (isRelease) "正式版" else "开发版"

        resValue("string", "git_branch", gitBranch())
        resValue("string", "git_hash", gitHash())
        resValue("string", "version_name", vName)
        resValue("string", "last_update_time", gitCommitDate())
        resValue("string", "build_status", buildStatus)


        val clientId = localProperties.getProperty("MICROSOFT_CLIENT_ID") ?: System.getenv("MICROSOFT_CLIENT_ID") ?: System.getenv("CLIENT_ID") ?: ""
        buildConfigField("String", "CLIENT_ID", "\"$clientId\"")

    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.12.0") // Add this for M3 themes
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-video:2.6.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.compose.foundation.layout)
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.maven:maven-artifact:3.8.6")
    implementation("commons-io:commons-io:2.16.1")
    implementation("commons-codec:commons-codec:1.16.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("org.ow2.asm:asm-all:5.0.4")
    implementation("com.github.oshi:oshi-core:6.3.0")
    implementation("androidx.browser:browser:1.8.0")
    implementation("dev.chrisbanes.haze:haze-jetpack-compose:0.5.4")
    implementation(libs.androidx.compose.ui.text)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
