#include "com_github_chenlijun99_jetbrainsassignment_zstd_Zstd.h"

#include "zstd.h"
#include "zstd_errors.h"

JNIEXPORT jlong JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_compress(
    JNIEnv *env, jclass cls, jbyteArray src, jbyteArray dst,
    jint compressionLevel) {
  jbyte *srcBuf = (*env)->GetByteArrayElements(env, src, NULL);
  jbyte *dstBuf = (*env)->GetByteArrayElements(env, dst, NULL);
  jsize srcSize = (*env)->GetArrayLength(env, src);
  jsize dstCapacity = (*env)->GetArrayLength(env, dst);

  size_t compressedSize = ZSTD_compress(dstBuf, (size_t)dstCapacity, srcBuf,
                                        (size_t)srcSize, (int)compressionLevel);

  (*env)->ReleaseByteArrayElements(
      env, src, srcBuf, JNI_ABORT); // JNI_ABORT to avoid copying back
  (*env)->ReleaseByteArrayElements(
      env, dst, dstBuf,
      0); // 0 to commit changes if any, though ZSTD writes directly

  return (jlong)compressedSize;
}

/*
 * Class:     com_github_chenlijun99_jetbrainsassignment_zstd_Zstd
 * Method:    compressCritical
 * Signature: ([B[BI)J
 */
JNIEXPORT jlong JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_compressCritical(
    JNIEnv *env, jclass cls, jbyteArray src, jbyteArray dst,
    jint compressionLevel) {
  // Using GetPrimitiveArrayCritical for direct access to array data
  void *srcBuf = (*env)->GetPrimitiveArrayCritical(env, src, NULL);
  void *dstBuf = (*env)->GetPrimitiveArrayCritical(env, dst, NULL);
  jsize srcSize = (*env)->GetArrayLength(env, src);
  jsize dstCapacity = (*env)->GetArrayLength(env, dst);

  if (srcBuf == NULL || dstBuf == NULL) {
    // Handle error: memory allocation failed or array access denied
    if (srcBuf != NULL)
      (*env)->ReleasePrimitiveArrayCritical(env, src, srcBuf, JNI_ABORT);
    if (dstBuf != NULL)
      (*env)->ReleasePrimitiveArrayCritical(env, dst, dstBuf, JNI_ABORT);
    return (jlong)ZSTD_error_memory_allocation;
  }

  size_t compressedSize = ZSTD_compress(dstBuf, (size_t)dstCapacity, srcBuf,
                                        (size_t)srcSize, (int)compressionLevel);

  (*env)->ReleasePrimitiveArrayCritical(
      env, src, srcBuf, JNI_ABORT); // JNI_ABORT to avoid copying back
  (*env)->ReleasePrimitiveArrayCritical(
      env, dst, dstBuf,
      0); // 0 to commit changes if any, though ZSTD writes directly

  return (jlong)compressedSize;
}

/*
 * Class:     com_github_chenlijun99_jetbrainsassignment_zstd_Zstd
 * Method:    getCompressBound
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_getCompressBound(
    JNIEnv *env, jclass cls, jlong srcSize) {
  return (jlong)ZSTD_compressBound((size_t)srcSize);
}

/*
 * Class:     com_github_chenlijun99_jetbrainsassignment_zstd_Zstd
 * Method:    isError
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_isError(JNIEnv *env,
                                                                  jclass cls,
                                                                  jlong code) {
  return (jboolean)ZSTD_isError((size_t)code);
}

/*
 * Class:     com_github_chenlijun99_jetbrainsassignment_zstd_Zstd
 * Method:    getErrorName
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_getErrorName(
    JNIEnv *env, jclass cls, jlong code) {
  const char *errorName = ZSTD_getErrorName((size_t)code);
  return (*env)->NewStringUTF(env, errorName);
}

/*
 * Class:     com_github_chenlijun99_jetbrainsassignment_zstd_Zstd
 * Method:    getErrorCode
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_getErrorCode(
    JNIEnv *env, jclass cls, jlong code) {
  return (jlong)ZSTD_getErrorCode((size_t)code);
}

#define JNI_ZSTD_ERROR(err, name)                                              \
  JNIEXPORT jlong JNICALL                                                      \
      Java_com_github_chenlijun99_jetbrainsassignment_zstd_Zstd_err##name(     \
          JNIEnv *env, jclass obj) {                                           \
    return ZSTD_error_##err;                                                   \
  }

JNI_ZSTD_ERROR(no_error, NoError)
JNI_ZSTD_ERROR(GENERIC, Generic)
JNI_ZSTD_ERROR(prefix_unknown, PrefixUnknown)
JNI_ZSTD_ERROR(version_unsupported, VersionUnsupported)
JNI_ZSTD_ERROR(frameParameter_unsupported, FrameParameterUnsupported)
JNI_ZSTD_ERROR(frameParameter_windowTooLarge, FrameParameterWindowTooLarge)
JNI_ZSTD_ERROR(corruption_detected, CorruptionDetected)
JNI_ZSTD_ERROR(checksum_wrong, ChecksumWrong)
JNI_ZSTD_ERROR(dictionary_corrupted, DictionaryCorrupted)
JNI_ZSTD_ERROR(dictionary_wrong, DictionaryWrong)
JNI_ZSTD_ERROR(dictionaryCreation_failed, DictionaryCreationFailed)
JNI_ZSTD_ERROR(parameter_unsupported, ParameterUnsupported)
JNI_ZSTD_ERROR(parameter_outOfBound, ParameterOutOfBound)
JNI_ZSTD_ERROR(tableLog_tooLarge, TableLogTooLarge)
JNI_ZSTD_ERROR(maxSymbolValue_tooLarge, MaxSymbolValueTooLarge)
JNI_ZSTD_ERROR(maxSymbolValue_tooSmall, MaxSymbolValueTooSmall)
JNI_ZSTD_ERROR(stage_wrong, StageWrong)
JNI_ZSTD_ERROR(init_missing, InitMissing)
JNI_ZSTD_ERROR(memory_allocation, MemoryAllocation)
JNI_ZSTD_ERROR(workSpace_tooSmall, WorkSpaceTooSmall)
JNI_ZSTD_ERROR(dstSize_tooSmall, DstSizeTooSmall)
JNI_ZSTD_ERROR(srcSize_wrong, SrcSizeWrong)
JNI_ZSTD_ERROR(dstBuffer_null, DstBufferNull)
