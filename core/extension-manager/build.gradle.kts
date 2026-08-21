plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:extension-api"))
}

kotlin {
    jvmToolchain(17)
}
