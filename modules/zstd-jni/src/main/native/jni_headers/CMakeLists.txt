if(CMAKE_SYSTEM_NAME MATCHES "Linux|Darwin")
  # For Unix-like systems (Linux, macOS)
  set(JNI_MD_DIR "${CMAKE_CURRENT_LIST_DIR}/unix")
elseif(CMAKE_SYSTEM_NAME MATCHES "Windows")
  set(JNI_MD_DIR "${CMAKE_CURRENT_LIST_DIR}/windows")
else()
  message(
    FATAL_ERROR
      "Unsupported target system for jni_md.h: '${CMAKE_SYSTEM_NAME}'. "
      "Cannot find platform-specific jni_md.h. "
      "Please ensure your target is 'Linux', 'Darwin', or 'Windows'.")
endif()

add_library(jni_headers INTERFACE)
target_include_directories(jni_headers INTERFACE ${CMAKE_CURRENT_LIST_DIR}
                                                 ${JNI_MD_DIR})
