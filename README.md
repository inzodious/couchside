# Couchside

Your Android TV app, from the couch, on your PC. Couchside runs an Android TV project's own Kotlin and Java in a normal
desktop window: resizable, maximisable, full screen with F11, drawn on the graphics card by Skia. There's no emulator
and no APK. Couchside reads the project's Gradle files and manifest, compiles its sources for the desktop, and runs
them on a small Android layer: Compose for Desktop, plus Android's own framework classes from Robolectric's
`android-all`. The app talks to its real servers, so what you see is real data.

It opens on the app's home screen when the app is a launcher, else on its launcher activity. Video and audio play
through the app's own player: Media3/ExoPlayer runs as it does on a TV, with FFmpeg doing the decoding (on the
graphics card through NVDEC or VA-API when it can).

## Use

```sh
couchside add myapp https://github.com/you/your-tv-app.git main android   # name, git URL or folder, branch, Gradle dir
couchside menu myapp        # adds Couchside to the app menu, opening myapp
couchside myapp             # pulls the branch, rebuilds if the project or Couchside changed, opens it
couchside myapp --app       # the app itself instead of its home screen
couchside                   # lists the projects
```

Link `bin/couchside` from somewhere on your PATH (`ln -s "$PWD/bin/couchside" ~/.local/bin/couchside`). It needs a
JDK 21 (`JAVA_HOME`) and an Android SDK with one platform installed (`ANDROID_HOME`, for `android.jar`).

Projects are listed in `~/.config/couchside/projects`. Under `~/.local/share/couchside/`: `src/NAME` is the clone of
a git project, `apps/NAME` the built app (`NAME.log` and `NAME.build.log` beside it), and `data/NAME` what the app
saves, such as sign-ins and settings.

Flags, after the project name:

| Flag | |
|---|---|
| `--app` | open the launcher activity even when the app has a home screen |
| `--fire-tv` | report a Fire TV (Amazon build values and the `amazon.hardware.fire_tv` feature) |
| `--light` | report 2 GB of RAM |
| `--fullscreen` | start full screen |
| `--set NAME=VALUE` | override a `BuildConfig` string of the app module, e.g. `--set API_URL=http://127.0.0.1:8080` |
| `--control=N` | accept test commands on 127.0.0.1:N (`runtime/src/main/kotlin/couchside/Control.kt`) |
| `--no-update` / `--rebuild` | open without pulling or building / build now and exit |

Environment: `COUCHSIDE_LOG=v|d|i|w|e` sets how much logcat reaches the log, `COUCHSIDE_JAVA_OPTS` adds JVM flags
(`-Dskiko.fps.enabled=true` logs the frame rate), `COUCHSIDE_SOFTWARE_VIDEO=1` keeps video decoding off the graphics
card, and `COUCHSIDE_MUTE=1` starts silent.

## The remote

The TV is 16:9 at any window size (a window of another shape shows bars, as the app's backgrounds are made for 16:9).

| Key or button | Remote |
|---|---|
| Arrows, Enter | D-pad, OK |
| Esc, right click | Back (Backspace too, when no text field takes it) |
| Left click | Point and press OK on whatever's under the pointer |
| Mouse wheel | D-pad up and down (with Shift, left and right) |
| Home or F1 | Home |
| F2 or the Menu key | Menu |
| F3 | Search |
| F5 / F6 / F7 | Rewind / Play-pause / Fast-forward |
| F9 / F10 | Channel down / up |
| F11 | Full screen |
| Ctrl+Q | Quit |

Typing goes straight into a text field once it has focus.

## How a project is read

`buildSrc/src/main/kotlin/TargetProject.kt` reads `settings.gradle(.kts)`, each module's build file (namespace,
application id, `buildConfigField`s, versions, dependencies through `gradle/libs.versions.toml`) and the app's manifest
(application class, launcher and home activities, icon, label). The app build (`app/build.gradle.kts`) then:

- resolves the libraries for the JVM: a Kotlin Multiplatform library gets its JVM build, an Android-only one its AAR's
  classes; Compose, lifecycle and activity come from Compose for Desktop instead;
- generates `R` and `BuildConfig`, and packs `res/` and `assets/` for the runtime;
- compiles the app's sources with the runtime (`runtime/`) and the SDK's `android.jar` for the API.

## When a project needs more

Most of what a TV app uses is covered by the runtime. What isn't goes in one of two places, never in the project:

- `libraries/<group>/<name>/`: a fix for a library, used by any project that uses it. `kotlin/` adds or replaces
  classes, `patches.txt` patches the library's own sources (from its sources jar), and `desktop` names a desktop build
  to use in its place, per platform (J2V8's Android-only V8 becomes Javet's, for example).
- A profile, for one project: `<profiles>/<name>/`, where `<profiles>` is `COUCHSIDE_PROFILES`, else
  `~/.config/couchside/profiles`, else `projects/` in this repo. `patches.txt` patches the project's sources,
  `replace/kotlin/` swaps whole files, `shim/` adds classes (for native code it calls, say), and `project.properties`
  sets the version (`versionName=1.0.{commits}`). Keeping profiles outside the repo keeps a private project private.

A `patches.txt` holds blocks like this (exact text within one line); the build fails if the old text is gone, so a
stale patch is noticed:

```
file: app/src/main/kotlin/com/example/tv/Graph.kt
- .diskCache { cache(context) }
+ .diskCache { cache(this@ExampleApplication) }
```

`./gradlew :app:libraries -Ptarget=DIR` lists what a project's libraries resolved to and what couldn't be.

## Not yet

- Protected (DRM) video, and passthrough of Dolby or DTS to a receiver (audio is mixed to stereo).
- Picture subtitles (PGS, VobSub) and libass drawing; text subtitles an app draws itself work.
- MediaSession and other Binder-backed services, voice input.
- A Windows build is untested (the code and native libraries are portable; the launcher is a bash script).

## Building by hand

```sh
JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk ./gradlew :app:stage -Ptarget=/path/to/android/project
```

The result is `build/targets/NAME/stage` (`-Pstage=DIR` puts it elsewhere); run it with
`java -cp "$(paste -sd: classpath.txt)" couchside.MainKt` from that folder.
