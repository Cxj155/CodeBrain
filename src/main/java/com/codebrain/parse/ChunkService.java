package com.codebrain.parse;

import com.codebrain.domain.entity.Chunk;
import com.codebrain.mapper.ChunkMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Path;
import java.util.List;

@Service
public class ChunkService {
    private final ChunkMapper chunkMapper;
    private final CachingChunker cachingChunker;

    public ChunkService(ChunkMapper chunkMapper, CachingChunker cachingChunker) {
        this.chunkMapper = chunkMapper;
        this.cachingChunker = cachingChunker;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Chunk> processFileChunk(Path filePath, Long fileId, String language) throws Exception {
        List<Chunk> chunkList = cachingChunker.getFileChunk(filePath, fileId, language);
        if (!chunkList.isEmpty()) {
            for (Chunk chunk : chunkList) {
                chunkMapper.insert(chunk);
            }
        }
        return chunkList;
    }

    public void clearFileCache(Path filePath) {
        cachingChunker.clearCache(filePath);
    }
}