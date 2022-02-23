plugins {
  id("com.android.test")
  id("org.jetbrains.kotlin.android")
}

// Note: We are not including our defaults from .buildscript as we not need the base Workflow
// dependencies that those include.

android {
  compileSdk = 32

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  defaultConfig {
    minSdk = 23
    targetSdk = 32

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    create("release") {
      isDebuggable = true
      signingConfig = getByName("debug").signingConfig
      proguardFile("baseline-proguard-rules.pro")
    }
  }

  targetProjectPath = ":samples:dungeon:app"
  experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
  implementation(Dependencies.Test.AndroidX.junitExt)
  implementation(Dependencies.Test.AndroidX.Espresso.core)
  implementation(Dependencies.Test.AndroidX.uiautomator)
  implementation(Dependencies.Test.AndroidX.Benchmark.macro)
}

androidComponents {
  beforeVariants(selector().all()) {
    it.enable = it.buildType == "release"
  }
}
