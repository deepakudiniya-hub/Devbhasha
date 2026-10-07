import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
  id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  // alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Automated Version Management via version.properties
val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { load(it) }
    }
}

val currentVersionCode = (versionProps.getProperty("versionCode")?.toIntOrNull() ?: 3)
val versionMajor = (versionProps.getProperty("versionMajor")?.toIntOrNull() ?: 3)
val versionMinor = (versionProps.getProperty("versionMinor")?.toIntOrNull() ?: 0)
val versionPatch = (versionProps.getProperty("versionPatch")?.toIntOrNull() ?: 0)
val currentVersionName = if (versionPatch > 0) "$versionMajor.$versionMinor.$versionPatch" else "$versionMajor.$versionMinor"

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.devbhasha.kxmpzq"
    minSdk = 24
    targetSdk = 36
    versionCode = 24
  
    versionName = "24.0"
    
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val customKeystore = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      if (file(customKeystore).exists()) {
        storeFile = file(customKeystore)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else {
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      isCrunchPngs = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
      
      // Additional R8 configuration for better stability
      setMatchingFallbacks(listOf("release"))
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  // Retrofit / OkHttp now resolved from the version catalog only (single source of truth)
  implementation(libs.converter.kotlinx.serialization)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.converter.moshi)
  implementation(libs.converter.gson)

  // Razorpay Checkout SDK
  implementation(libs.razorpay.checkout)

  // Credentials
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.agora.voice) {
    exclude(group = "io.agora.infra", module = "aosl")
  }
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)

  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.functions)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  // "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}

// Tasks to automatically bump version in version.properties
tasks.register("incrementVersionCode") {
  description = "Increments the versionCode in version.properties"
  doLast {
    val nextCode = currentVersionCode + 1
    versionProps.setProperty("versionCode", nextCode.toString())
    versionProps.setProperty("versionMajor", versionMajor.toString())
    versionProps.setProperty("versionMinor", versionMinor.toString())
    versionProps.setProperty("versionPatch", (versionPatch + 1).toString())
    FileOutputStream(versionPropsFile).use { versionProps.store(it, "Updated by incrementVersionCode task") }
    println("Updated version: versionCode=$nextCode, versionName=$versionMajor.$versionMinor.${versionPatch + 1}")
  }
}

tasks.register("incrementVersionMinor") {
  description = "Increments minor version and versionCode in version.properties"
  doLast {
    val nextCode = currentVersionCode + 1
    val nextMinor = versionMinor + 1
    versionProps.setProperty("versionCode", nextCode.toString())
    versionProps.setProperty("versionMajor", versionMajor.toString())
    versionProps.setProperty("versionMinor", nextMinor.toString())
    versionProps.setProperty("versionPatch", "0")
    FileOutputStream(versionPropsFile).use { versionProps.store(it, "Updated by incrementVersionMinor task") }
    println("Updated version: versionCode=$nextCode, versionName=$versionMajor.$nextMinor")
  }
}
