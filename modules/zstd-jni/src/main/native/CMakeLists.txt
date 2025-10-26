cmake_minimum_required(VERSION 3.28)

# Export compile_commands.json by default
set(CMAKE_EXPORT_COMPILE_COMMANDS 1)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/cmake")

set(EXTERNAL_SUBMODULES_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../../../external")

project(zstd-jni LANGUAGES C CXX ASM)

# Weird. I thought CMake did that automatically...
if(CMAKE_SYSTEM_NAME MATCHES "Windows")
  set(CMAKE_SHARED_LIBRARY_PREFIX "")
  set(CMAKE_STATIC_LIBRARY_PREFIX "")
endif()

set(TARGET_NAME "${PROJECT_NAME}")

add_library(${TARGET_NAME} SHARED "")

target_sources(${TARGET_NAME} PRIVATE zstd.c)
target_include_directories(${TARGET_NAME} PRIVATE ${JNI_INCLUDE_PATH})
target_compile_options(${TARGET_NAME} PRIVATE -Wall -Wextra -Wpedantic -Werror
                                              -Wno-unused-parameter)
# Include jni headers
add_subdirectory(jni_headers)
target_link_libraries(${TARGET_NAME} PRIVATE jni_headers)

# Include zstd
set(ZSTD_BUILD_STATIC ON)
set(ZSTD_BUILD_SHARED OFF)
set(ZSTD_BUILD_DECOMPRESSION OFF)
set(ZSTD_BUILD_DICTBUILDER OFF)
set(ZSTD_BUILD_PROGRAMS OFF)
# NOTE: `cmake_policy(SET CMP0077 NEW)` doesn't work See
# https://stackoverflow.com/questions/66340703/how-to-force-cmake-to-use-the-new-version-of-cmp0077-allow-options-to-be-set-fr
set(CMAKE_POLICY_DEFAULT_CMP0077 NEW)
add_subdirectory(${EXTERNAL_SUBMODULES_DIR}/zstd/build/cmake/ "zstd")

target_link_libraries(${TARGET_NAME} PRIVATE libzstd_static)
