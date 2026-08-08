package com.codebrain.parse.exception;

public abstract class ParseException extends Exception {

    public ParseException(String msg) {
        super(msg);
    }

    public ParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public abstract boolean isRetryable();
}
