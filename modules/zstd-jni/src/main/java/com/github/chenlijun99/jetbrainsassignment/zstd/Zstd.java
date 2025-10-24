package com.github.chenlijun99.jetbrainsassignment.zstd;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.scijava.nativelib.NativeLoader;

/**
 * Provides Java bindings for the Zstandard (Zstd) compression library.
 * This class includes native methods for compression and convenience wrappers
 * for easier use with byte arrays and ByteBuffers.
 *
 * <p><h3>Critical Methods</h3>
 * Methods ending with "Critical" (e.g., {@code compressCritical}) leverage JNI's
 * {@code GetPrimitiveArrayCritical} mechanism. This aims to provide maximum performance
 * by potentially avoiding data copies between the Java heap and native memory during
 * JNI calls. However, depending on the JVM implementation (especially one that
 * doesn't implement memory pinning), the GC may be disabled during this
 * "critical" section to ensure that the memory is not moved while being
 * processed by the native code.
 * Users should assess whether the performance benefits outweigh the potential
 * implications of blocking the GC for their specific use case.
 * </p>
 *
 * <p><h3>Return Types of Wrapper Methods</h3>
 * This class provides two sets of wrapper methods for compression, differentiated by their return types:
 * <ul>
 *     <li>Methods returning {@code byte[]}: These methods allocate a new byte array
 *         and copy the compressed data into it. This provides a self-contained result
 *         but involves an additional memory allocation and copy operation.</li>
 *     <li>Methods returning {@code ByteBuffer}: These methods return a {@link java.nio.ByteBuffer}
 *         that wraps the internally allocated destination array. The buffer's limit is set to
 *         the actual compressed size, effectively providing a view or slice of the compressed data
 *         without an additional copy. This can be more memory-efficient if the underlying
 *         buffer is managed carefully, but the returned ByteBuffer still holds a reference
 *         to the potentially larger internal buffer.</li>
 * </ul>
 * </p>
 */
public class Zstd {
    static {
        try {
            NativeLoader.loadLibrary("zstd-jni");
        } catch (IOException e) {
            System.err.println(e);
            System.err.println("Failed to load native library 'zstd-jni'");
        }
    }

    /**
     * Compresses a byte array using Zstandard algorithm. This method may copy the source array
     * data to native memory before compression.
     */
    public static native long compress(byte[] src, byte[] dst, int compressionLevel);
    /**
     * Compresses a byte array using Zstandard algorithm, leveraging JNI's {@code GetPrimitiveArrayCritical}
     * for direct access to array data. Refer to the class Javadoc for characteristics of "critical" methods.
     */
    public static native long compressCritical(byte[] src, byte[] dst, int compressionLevel);
    public static native long getCompressBound(long srcSize);
    public static native boolean isError(long code);
    public static native String  getErrorName(long code);
    public static native long    getErrorCode(long code);

    public static native long errNoError();
    public static native long errGeneric();
    public static native long errPrefixUnknown();
    public static native long errVersionUnsupported();
    public static native long errFrameParameterUnsupported();
    public static native long errFrameParameterWindowTooLarge();
    public static native long errCorruptionDetected();
    public static native long errChecksumWrong();
    public static native long errDictionaryCorrupted();
    public static native long errDictionaryWrong();
    public static native long errDictionaryCreationFailed();
    public static native long errParameterUnsupported();
    public static native long errParameterOutOfBound();
    public static native long errTableLogTooLarge();
    public static native long errMaxSymbolValueTooLarge();
    public static native long errMaxSymbolValueTooSmall();
    public static native long errStageWrong();
    public static native long errInitMissing();
    public static native long errMemoryAllocation();
    public static native long errWorkSpaceTooSmall();
    public static native long errDstSizeTooSmall();
    public static native long errSrcSizeWrong();
    public static native long errDstBufferNull();

    // --- Private helper structures and methods to reduce repetition ---

    /**
     * Functional interface for native compression methods.
     */
    @FunctionalInterface
    private interface NativeCompressor {
        long compress(byte[] src, byte[] dst, int compressionLevel);
    }

    private record CompressionResult(byte[] buffer, long actualSize) {}

    /**
     * Common compression logic for both standard and critical compression methods.
     * Handles input validation, buffer allocation, native call, and error checking.
     *
     * @param src The source byte array.
     * @param compressionLevel The compression level.
     * @param nativeCompressor The specific native compression function (e.g., Zstd::compress or Zstd::compressCritical).
     * @param nativeMethodName A descriptive name for the *native* method being called (e.g., "compress", "compressCritical").
     * @return A CompressionResult containing the allocated buffer and the actual compressed size.
     */
    private static CompressionResult doCommonCompression(
        byte[] src,
        int compressionLevel,
        NativeCompressor nativeCompressor,
        String nativeMethodName
    ) {
        if (src == null) {
            throw new IllegalArgumentException("Source buffer cannot be null for Zstd." + nativeMethodName + ".");
        }
        if (src.length == 0) {
            return new CompressionResult(new byte[0], 0);
        }

        long bound = getCompressBound(src.length);
        if (bound > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("Compressed data bound (" + bound + ") exceeds maximum array size for Zstd." + nativeMethodName + ".");
        }
        byte[] dst = new byte[(int) bound];
        long compressedSize = nativeCompressor.compress(src, dst, compressionLevel);

        if (isError(compressedSize)) {
            long errorCode = getErrorCode(compressedSize);
            throw new ZstdException(errorCode);
        }
        return new CompressionResult(dst, compressedSize);
    }

    // --- Public wrapper methods returning byte[] (perform a copy) ---

    /**
     * Compresses a byte array using Zstandard algorithm.
     * Refer to the class Javadoc for characteristics of methods returning {@code byte[]}.
     *
     * @param src The source byte array to compress.
     * @param compressionLevel The desired compression level.
     * @return A new byte array containing the compressed data.
     */
    public static byte[] compress(byte[] src, int compressionLevel) {
        CompressionResult result = doCommonCompression(src, compressionLevel, Zstd::compress, "compress");
        if (result.actualSize == 0) {
            return new byte[0];
        }
        byte[] finalCompressedData = new byte[(int) result.actualSize];
        System.arraycopy(result.buffer, 0, finalCompressedData, 0, (int) result.actualSize);
        return finalCompressedData;
    }

    /**
     * Compresses a byte array using Zstandard algorithm, leveraging JNI's {@code GetPrimitiveArrayCritical}.
     * Refer to the class Javadoc for characteristics of "critical" methods and methods returning {@code byte[]}.
     *
     * @param src The source byte array to compress.
     * @param compressionLevel The desired compression level.
     * @return A new byte array containing the compressed data.
     */
    public static byte[] compressCritical(byte[] src, int compressionLevel) {
        CompressionResult result = doCommonCompression(src, compressionLevel, Zstd::compressCritical, "compressCritical");
        if (result.actualSize == 0) {
            return new byte[0];
        }
        byte[] finalCompressedData = new byte[(int) result.actualSize];
        System.arraycopy(result.buffer, 0, finalCompressedData, 0, (int) result.actualSize);
        return finalCompressedData;
    }

    // --- Public wrapper methods returning ByteBuffer (no data copy for return value) ---

    /**
     * Compresses a byte array using Zstandard algorithm.
     * Refer to the class Javadoc for characteristics of methods returning {@code ByteBuffer}.
     *
     * @param src The source byte array to compress.
     * @param compressionLevel The desired compression level.
     * @return A {@code ByteBuffer} containing the compressed data.
     */
    public static ByteBuffer compressToByteBuffer(byte[] src, int compressionLevel) {
        CompressionResult result = doCommonCompression(src, compressionLevel, Zstd::compress, "compressToByteBuffer");
        return ByteBuffer.wrap(result.buffer, 0, (int) result.actualSize);
    }

    /**
     * Compresses a byte array using Zstandard algorithm, leveraging JNI's {@code GetPrimitiveArrayCritical}.
     * Refer to the class Javadoc for characteristics of "critical" methods and methods returning {@code ByteBuffer}.
     *
     * @param src The source byte array to compress.
     * @param compressionLevel The desired compression level.
     * @return A {@code ByteBuffer} containing the compressed data.
     */
    public static ByteBuffer compressCriticalToByteBuffer(byte[] src, int compressionLevel) {
        CompressionResult result = doCommonCompression(src, compressionLevel, Zstd::compressCritical, "compressCriticalToByteBuffer");
        return ByteBuffer.wrap(result.buffer, 0, (int) result.actualSize);
    }
}
