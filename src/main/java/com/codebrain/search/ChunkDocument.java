package com.codebrain.search;

import java.util.List;

public record ChunkDocument(
        Long chunkId,
        Long fileId,
        Long repositoryId,
        String relativePath,
        String language,
        String kind,
        String name,
        Integer startLine,
        Integer endLine,
        String content,
        String contentHash,
        List<Float> embedding
) {}