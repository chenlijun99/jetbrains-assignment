cmake_minimum_required(VERSION 3.28)

# Export compile_commands.json by default
set(CMAKE_EXPORT_COMPILE_COMMANDS 1)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake")

set(EXTERNAL_SUBMODULES_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../../../external")

project(zstd-jni LANGUAGES C CXX ASM)

# Find the jni.h and jni_md.h headers from the installed JDK.
#
# `find_package(JNI REQUIRED)` tries to "find too much stuff", which doesn't
# work in a cross-compilation setting. We only need the headers. We could even
# copy them and vendor them ourselves. But, what about licensing?
#
# The following code is mostly inspired by the `FindJNI.cmake` distributed with
# CMake
if(DEFINED ENV{JAVA_HOME})
  set(JAVA_HOME "$ENV{JAVA_HOME}")
  message(STATUS "JAVA_HOME found: ${JAVA_HOME}")
else()
  message(FATAL_ERROR "JAVA_HOME environment variable not set.")
endif()
find_path(JNI_INCLUDE_PATH jni.h ${JAVA_HOME}/include
          DOC "JNI include directory")
if(NOT JNI_INCLUDE_PATH)
  message(FATAL_ERROR "Unable to find path for `jni.h`")
endif()

set(TARGET_NAME "${PROJECT_NAME}")

add_library(${TARGET_NAME} SHARED "")

target_sources(${TARGET_NAME} PRIVATE zstd.c)
target_include_directories(${TARGET_NAME} PRIVATE ${JNI_INCLUDE_PATH})
target_compile_options(${TARGET_NAME} PRIVATE -Wall -Wextra -Wpedantic -Werror
                                              -Wno-unused-parameter)

# Include zstd
set(ZSTD_BUILD_STATIC ON)
set(ZSTD_BUILD_SHARED OFF)
set(ZSTD_BUILD_DECOMPRESSION OFF)
set(ZSTD_BUILD_DICTBUILDER OFF)
set(ZSTD_BUILD_PROGRAMS OFF)
# NOTE: 
# `cmake_policy(SET CMP0077 NEW)` doesn't work
# See https://stackoverflow.com/questions/66340703/how-to-force-cmake-to-use-the-new-version-of-cmp0077-allow-options-to-be-set-fr
set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)
add_subdirectory(${EXTERNAL_SUBMODULES_DIR}/zstd/build/cmake/ "zstd")

target_link_libraries(${TARGET_NAME} PRIVATE libzstd_static)
