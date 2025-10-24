# GLIBC 2.28 is the minimum glibc supported by Intellij Idea
# https://www.jetbrains.com/help/idea/installation-guide.html
set(ZIG_TARGET "x86_64-linux-gnu.2.28")
include(
  ${CMAKE_CURRENT_LIST_DIR}/../../../../../../../external/zig-cross/cmake/zig-toolchain.cmake
)
