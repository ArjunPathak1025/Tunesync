plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(project(":core:model"))
}

kotlin {
    jvmToolchain(17)
}
