plugins {
  id("com.android.library")
  kotlin("android")
  id("org.jetbrains.dokka")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

// This module is not published, since it's just internal testing utilities.
apply(from = rootProject.file(".buildscript/configure-android-defaults.gradle"))

dependencies {
  api(project(":workflow-ui:core-android"))

  api(libs.androidx.appcompat)
  api(libs.androidx.test.espresso.core)
  api(libs.kotlin.jdk6)
  api(libs.truth)
}
