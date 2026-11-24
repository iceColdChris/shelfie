plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(ShelfieBuild.JVM_TOOLCHAIN)
}
