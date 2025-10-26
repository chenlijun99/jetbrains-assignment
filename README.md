# clj-zstd-util

![Build](https://github.com/chenlijun99/jetbrains-assignment/workflows/Build/badge.svg?branch=main)

<!-- Plugin description -->
This plugin introduces an utility action to easily compress the currently opened file using the Zstandard (zstd) native library.
<!-- Plugin description end -->

https://github.com/user-attachments/assets/3887fa2b-adb0-41d2-9458-f0e540f99542

## Installation

Download the [latest release](https://github.com/chenlijun99/jetbrains-assignment/releases/latest) and install it manually using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

## Technical details

* `src/`: provides a simple Intellij Platform plugin that adds two actions to the editor:
    * Compress the current file using zstd and save it in a file in the same directory, which has as name the same of the original file but with *.zst appended to the end.
    * Compress the current file using zstd and save it in a user-chosen location using a file choose. Especially useful when the current file's folder is not writable (e.g. when opening a file from a JAR archive).
    * Relies on `modules/zstd-jni/` for zstd compression.
* `modules/zstd-jni/`: provides the JNI bindings for the zstd native library.
    * Uses CMake and `zig cc` for cross-compilation of the JNI bindings for zstd
    * JNI binding statically links `zstd`.
    * The cross-compiled JNI bindings for all the supported platforms are bundled into `zstd-jni.jar`.
    * [scijava/native-lib-loader](https://github.com/scijava/native-lib-loader) is used to extract the appropriate JNI binding shared library for the current platform from the JAR archive and load it on the JVM.

See also [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to setup the development environment.

## Known issues

* [Plugin fails to dynamically unload after action "Compress the current file with zstd (Save As)" is used](https://github.com/chenlijun99/jetbrains-assignment/issues/16)
