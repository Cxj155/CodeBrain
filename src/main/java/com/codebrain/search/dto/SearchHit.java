package com.codebrain.search.dto;

public record SearchHit(
        String id,
        Long chunkId,
        Long fileId,
        String relativePath,
        String language,
        String kind,
        String name,
        String content,
        Integer startLine,
        Integer endLine,
        double rawScore,
        int rank,
        String source,
        ScoreBreakdown scoreBreakdown
) {}

