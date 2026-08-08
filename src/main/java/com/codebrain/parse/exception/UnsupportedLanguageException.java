package com.codebrain.parse.exception;

public class UnsupportedLanguageException extends ParseException {
    public UnsupportedLanguageException(String message) {
        super(message);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}
