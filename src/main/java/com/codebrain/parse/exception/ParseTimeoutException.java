package com.codebrain.parse.exception;

public class ParseTimeoutException extends ParseException {
    public ParseTimeoutException(String message) {
        super(message);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
