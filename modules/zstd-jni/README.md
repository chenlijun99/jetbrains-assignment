# zstd-jni

Minimal JNI bindings for zstd

## Build and test

* `:modules:zstd-jni:jar`: builds the zstd JNI binding for all the supported platforms and packages them into the jar.
* `:modules:zstd-jni:jarLocal`: builds the zstd JNI binding only for the current platforms and packages it into the jar.
* `:modules:zstd-jni:test`: builds the zstd JNI binding only for the current platforms and runs unit tests.

## Build bindings manually

We use `zig cc` for cross-compilation.

```sh
$ cd src/main/native
# Feel free to choose another build directory and other toolchain files
# in cmake/toolchains
$ cmake -B build/linux/x86-64 --toolchain cmake/toolchains/x86_64-linux-gnu.cmake
$ cmake --build build/linux/x86-64
```

## JNI headers

Since we perform cross-compilation of our zstd JNI binding, which depend on the platform specific JNI header `jni_md.h` from JDK, we need to acquire different JNI headers depending on cross-compilation target. The JNI header that comes with the JDK installed on the current platform (where we perform the build) is not enough.

To make things simpler, we vendor the platform specific JNI headers in `./src/main/native/jni_headers`, which are downloaded using the script `./src/main/native/download_jni_headers.sh`.

## Credits

* Heavily inspired by [luben/zstd-jni](https://github.com/luben/zstd-jni)
