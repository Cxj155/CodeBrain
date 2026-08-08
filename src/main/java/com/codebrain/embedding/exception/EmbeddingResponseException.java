package com.codebrain.embedding.exception;

public class EmbeddingResponseException extends EmbeddingException {
    public EmbeddingResponseException(String message) {
        super(message);
    }

    public EmbeddingResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}