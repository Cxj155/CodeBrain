package com.codebrain.search;
import com.codebrain.domain.entity.Chunk;
import com.codebrain.embedding.EmbeddingService;
import com.codebrain.parse.CachingChunker;
import com.codebrain.parse.ChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkIndexServiceImpl implements ChunkIndexService {
    private final ElasticsearchChunkRepository esRepo;
    private final EmbeddingService embeddingService;
    private final ChunkService chunkService;
    private final CachingChunker cachingChunker;

    @Override
    public void indexFile(Path path, Long repositoryId, Long fileId, String language) throws Exception {
        List<Chunk> chunkList = chunkService.processFileChunk(path, fileId, language);
        fixChunkLineNumber(chunkList);
        try {
            indexChunks(repositoryId, path.toString(), chunkList);
        } catch (IOException e) {
            log.error("文件{} ES索引写入失败 fileId={}", path, fileId, e);
            throw new Exception("ES索引同步失败", e);
        }
    }

    @Override
    public void reindexFile(Path path, Long repositoryId, Long fileId, String language) throws Exception {
        try{
            deleteByFileId(fileId);
        }catch (Exception e){
            log.error("清理ES旧索引异常 fileId={}",fileId,e);
        }

        cachingChunker.clearCache(path);
        List<Chunk> chunkList = chunkService.processFileChunk(path, fileId, language);
        fixChunkLineNumber(chunkList);
        try {
            indexChunks(repositoryId, path.toString(), chunkList);
        } catch (IOException e) {
            log.error("文件{} 重新索引ES失败 fileId={}", path, fileId, e);
            throw new Exception("ES重新索引失败", e);
        }
    }

    @Override
    public void deleteByFileId(Long fileId) {
        try {
            esRepo.deleteByFileId(fileId);
        } catch (Exception e) {
            log.error("删除fileId={} ES索引文档失败", fileId, e);
        }
    }

    private void fixChunkLineNumber(List<Chunk> chunkList) {
        for (Chunk chunk : chunkList) {
            if (chunk.getStartLine() == null || chunk.getStartLine() <= 0) {
                chunk.setStartLine(1);
            }
            if (chunk.getEndLine() == null || chunk.getEndLine() <= 0) {
                chunk.setEndLine(1);
            }
        }
    }

    private void indexChunks(Long repositoryId, String relativePath, List<Chunk> chunks) throws IOException {
        if (CollectionUtils.isEmpty(chunks)) return;
        List<String> textList = new ArrayList<>();
        for (Chunk c : chunks) {
            textList.add(c.getContent());
        }
        List<double[]> embeddingList = embeddingService.embed(textList);
        List<ChunkDocument> docList = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if(chunk.getId() == null){
                log.warn("分片主键为空，跳过ES写入 fileId={}", chunk.getFileId());
                continue;
            }
            double[] vec = embeddingList.get(i);
            List<Float> floatList = new ArrayList<>();
            for (double v : vec) {
                floatList.add((float) v);
            }
            ChunkDocument doc = new ChunkDocument(
                    chunk.getId(),
                    chunk.getFileId(),
                    repositoryId,
                    relativePath,
                    chunk.getLanguage(),
                    chunk.getKind().name(),
                    chunk.getName(),
                    chunk.getStartLine(),
                    chunk.getEndLine(),
                    chunk.getContent(),
                    chunk.getContentHash(),
                    floatList
            );
            docList.add(doc);
        }
        esRepo.bulkUpsert(docList);
        log.info("文件分片完成写入ES path={}", relativePath);
    }
}