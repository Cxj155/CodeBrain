package com.codebrain.search;

import com.codebrain.domain.entity.Chunk;
import java.nio.file.Path;
import java.util.List;

public interface ChunkIndexService {
    void indexFile(Path path, Long repositoryId, Long fileId, String language) throws Exception;
    void reindexFile(Path path, Long repositoryId, Long fileId, String language) throws Exception;
    void deleteByFileId(Long fileId);

}