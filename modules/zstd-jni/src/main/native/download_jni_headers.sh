#!/usr/bin/env bash

# Set the OpenJDK Git tag for the desired JNI header version
JNI_JDK_TAG="jdk-21+0"
BASE_DOWNLOAD_DIR="$(dirname "$0")/jni_headers/"

echo "--- Downloading JNI Headers for OpenJDK ${JNI_JDK_TAG} ---"
echo "Headers will be placed under: ${BASE_DOWNLOAD_DIR}"
echo ""

# Create the base download directory
mkdir -p "${BASE_DOWNLOAD_DIR}"

# Base URL for OpenJDK's src/java.base module (raw content from GitHub)
OPENJDK_BASE_RAW_URL="https://raw.githubusercontent.com/openjdk/jdk/${JNI_JDK_TAG}/src/java.base/"

# --- Download shared jni.h ---
JNI_H_URL="${OPENJDK_BASE_RAW_URL}share/native/include/jni.h"
JNI_H_DESTINATION="${BASE_DOWNLOAD_DIR}/jni.h"

echo "Downloading shared jni.h from: ${JNI_H_URL}"

if ! curl -sSL -o "${JNI_H_DESTINATION}" "${JNI_H_URL}"; then
	echo "Error: Failed to download jni.h. Please check the JDK tag and URL."
	exit 1
fi

# --- Download platform-specific jni_md.h for Unix ---
UNIX_JNI_MD_DIR="${BASE_DOWNLOAD_DIR}/unix"
mkdir -p "${UNIX_JNI_MD_DIR}"

UNIX_JNI_MD_H_URL="${OPENJDK_BASE_RAW_URL}unix/native/include/jni_md.h"
UNIX_JNI_MD_H_DESTINATION="${UNIX_JNI_MD_DIR}/jni_md.h"

echo "Downloading unix jni_md.h from: ${UNIX_JNI_MD_H_URL}"

if ! curl -sSL -o "${UNIX_JNI_MD_H_DESTINATION}" "${UNIX_JNI_MD_H_URL}"; then
	echo "Error: Failed to download unix jni_md.h. Please check the JDK tag and URL."
	exit 1
fi

# --- Download platform-specific jni_md.h for Windows ---
WINDOWS_JNI_MD_DIR="${BASE_DOWNLOAD_DIR}/windows"
mkdir -p "${WINDOWS_JNI_MD_DIR}"

WINDOWS_JNI_MD_H_URL="${OPENJDK_BASE_RAW_URL}windows/native/include/jni_md.h"
WINDOWS_JNI_MD_H_DESTINATION="${WINDOWS_JNI_MD_DIR}/jni_md.h"

echo "Downloading windows jni_md.h from: ${WINDOWS_JNI_MD_H_URL}"

if ! curl -sSL -o "${WINDOWS_JNI_MD_H_DESTINATION}" "${WINDOWS_JNI_MD_H_URL}"; then
	echo "Error: Failed to download windows jni_md.h. Please check the JDK tag and URL."
	exit 1
fi

echo ""
echo "All JNI headers downloaded successfully."
echo "Shared jni.h is in: ${BASE_DOWNLOAD_DIR}/"
echo "Unix jni_md.h is in: ${UNIX_JNI_MD_DIR}/"
echo "Windows jni_md.h is in: ${WINDOWS_JNI_MD_DIR}/"
echo ""
