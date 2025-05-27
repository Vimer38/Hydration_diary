// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.devtoolsKsp) apply false
    kotlin("jvm")
    id("com.google.gms.google-services") version "4.4.1" apply false
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
