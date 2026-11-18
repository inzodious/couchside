import java.util.Properties

// The simulated TV: Android's framework where a desktop has to stand in (graphics on Skia, Context, resources,
// activities, keys, Handler/Looper on the AWT thread) and the window that shows it. Nothing here knows the app.

plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

fun sdkDir(): File = File(
    System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT") ?: "${System.getProperty("user.home")}/Android/Sdk",
)

/** Android's API (android.jar of the newest SDK platform installed): classes only, for compiling against. */
val androidApi = tasks.register<Jar>("androidApi") {
    val platform = sdkDir().resolve("platforms").listFiles { f -> f.resolve("android.jar").exists() }
        ?.maxByOrNull { it.name.removePrefix("android-").toDoubleOrNull() ?: 0.0 }
        ?: error("No Android SDK platform under ${sdkDir()}/platforms. Install one (sdkmanager \"platforms;android-36\") or set ANDROID_HOME.")
    from(zipTree(platform.resolve("android.jar"))) { include("android/**", "dalvik/**", "com/android/**") }
    archiveFileName.set("android-api.jar")
    destinationDirectory.set(layout.buildDirectory.dir("android-api"))
}

val androidApiJar: Configuration by configurations.creating { isCanBeConsumed = true; isCanBeResolved = false }
artifacts { add("androidApiJar", androidApi) }

kotlin {
    jvmToolchain(21)
    compilerOptions { freeCompilerArgs.addAll("-Xskip-prerelease-check", "-opt-in=kotlin.RequiresOptIn") }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:none"))
}

val ffmpeg = "8.1.2-1.5.14"
val javacpp = "1.5.14"

/** The native libraries' platform, as JavaCPP names it. */
val nativePlatform: String = run {
    val os = System.getProperty("os.name").lowercase()
    val arch = if (System.getProperty("os.arch") in setOf("aarch64", "arm64")) "arm64" else "x86_64"
    when {
        os.startsWith("windows") -> "windows-$arch"
        os.startsWith("mac") -> "macosx-$arch"
        else -> "linux-$arch"
    }
}

dependencies {
    api(compose.desktop.currentOs)
    api(compose.foundation)
    api(compose.ui)
    api(compose.animation)
    api("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
    api("org.json:json:20250517")
    api("net.sf.kxml:kxml2:2.3.0")
    // Video and audio decoding (MediaCodec, android/media/): FFmpeg's decoders, with the native libraries for this PC.
    api("org.bytedeco:ffmpeg:$ffmpeg")
    runtimeOnly("org.bytedeco:ffmpeg:$ffmpeg:$nativePlatform")
    runtimeOnly("org.bytedeco:javacpp:$javacpp:$nativePlatform")
    compileOnly(files(androidApi.map { it.archiveFile }))
    // Under everything at run time: Android 14's own framework compiled for the JVM (Robolectric's android-all), so
    // pure-Java framework classes run as Android's code. Native ones fail there and the runtime covers them.
    runtimeOnly("org.robolectric:android-all:14-robolectric-10818077")
}
