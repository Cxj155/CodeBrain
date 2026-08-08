package com.codebrain.embedding.exception;

public class EmbeddingTransientException extends EmbeddingException {
    public EmbeddingTransientException(String message) {
        super(message);
    }

    public EmbeddingTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}