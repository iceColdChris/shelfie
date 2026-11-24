// Single source for the SDK and JVM levels every convention plugin applies, so the
// app and library modules cannot drift apart.
object ShelfieBuild {
    const val COMPILE_SDK = 37
    const val COMPILE_SDK_MINOR = 2
    const val MIN_SDK = 24
    const val TARGET_SDK = 37
    const val JVM_TOOLCHAIN = 17
}
