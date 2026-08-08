package com.codebrain.parse.exception;

public class ParseTooLargeException extends ParseException {
    public ParseTooLargeException(String message) {
        super(message);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}
