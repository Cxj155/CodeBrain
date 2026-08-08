package com.codebrain.parse.exception;

public class ParseCrashException extends ParseException {
    public ParseCrashException(String message) {
        super(message);
    }

    public ParseCrashException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
