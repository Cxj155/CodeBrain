package com.codebrain.embedding.exception;

public class EmbeddingRequestException extends EmbeddingException {
    public EmbeddingRequestException(String message) {
        super(message);
    }

    public EmbeddingRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}