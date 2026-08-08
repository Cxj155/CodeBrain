package com.codebrain.embedding.exception;

public class EmbeddingTimeoutException extends EmbeddingException {
    public EmbeddingTimeoutException(String message) {
        super(message);
    }

    public EmbeddingTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}