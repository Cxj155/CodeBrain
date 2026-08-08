package com.codebrain.parse;

public record ParseResult(
        String language,
        Long nodeCount,
        long elapsedMs,
        ParseStatus status,
        String stdout
) {}
