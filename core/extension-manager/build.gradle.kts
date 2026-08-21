plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:extension-api"))
}

kotlin {
    jvmToolchain(17)
}
