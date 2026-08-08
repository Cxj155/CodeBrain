package com.codebrain.parse.exception;

public class ParseSyntaxException extends ParseException {
    public ParseSyntaxException(String message) {
        super(message);
    }

    @Override
    public boolean isRetryable() {
        return false;
    }
}
