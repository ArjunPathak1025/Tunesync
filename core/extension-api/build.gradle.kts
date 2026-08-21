plugins {
    id("org.jetbrains.kotlin.jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:model"))
}

kotlin {
    jvmToolchain(17)
}
