package com.github.chenlijun99.jetbrainsassignment.zstd;

public class ZstdException extends RuntimeException {
    private final long errorCode;

    public ZstdException(long errorCode) {
        // Construct the message once using Zstd.getErrorName(errorCode)
        super("Zstd error: " + Zstd.getErrorName(errorCode) + " (code: " + errorCode + ")");
        this.errorCode = errorCode;
    }

    public long getErrorCode() {
        return errorCode;
    }

    // Provides the error name by delegating to Zstd.getErrorName
    public String getErrorName() {
        return Zstd.getErrorName(errorCode);
    }
}