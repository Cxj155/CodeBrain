package com.codebrain.parse;
import com.codebrain.common.BusinessException;
import com.codebrain.domain.entity.Chunk;
import com.codebrain.domain.entity.SNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
@Service
public class CachingChunker {
    private final ConcurrentHashMap<String, List<Chunk>> chunkCache = new ConcurrentHashMap<>();
    private final CodeParserFactory codeParserFactory;
    private final SExpressionParser sExpressionParser;
    private final ParseRetryUtil retryUtil;
    private final Chunker chunker;

    public CachingChunker(CodeParserFactory codeParserFactory,
                          SExpressionParser sExpressionParser,
                          ParseRetryUtil retryUtil,
                          Chunker chunker) {
        this.codeParserFactory = codeParserFactory;
        this.sExpressionParser = sExpressionParser;
        this.retryUtil = retryUtil;
        this.chunker = chunker;
    }

    public List<Chunk> getFileChunk(Path filePath, Long fileId, String language) {
        String fileHash = calcFileHash(filePath);
        if (chunkCache.containsKey(fileHash)) {
            return chunkCache.get(fileHash);
        }
        CodeParser parser;
        try {
            parser = codeParserFactory.getParser(filePath);
        } catch (Exception e) {
            throw new BusinessException(50001, "获取代码解析器失败，不支持该文件编程语言", e);
        }
        ParseResult parseResult;
        try {
            parseResult = retryUtil.parseWithRetry(parser, filePath);
        } catch (Exception e) {
            throw new BusinessException(50002, "文件AST解析失败，文件路径错误或源码存在语法错误", e);
        }
        SNode rootNode;
        try {
            String astText = parseResult.stdout();
            rootNode = sExpressionParser.parse(astText);
        } catch (Exception e) {
            throw new BusinessException(50003, "AST结构化解析失败", e);
        }
        String fullContent;
        try {
            fullContent = Files.readString(filePath);
        } catch (Exception e) {
            throw new BusinessException(50006, "读取文件源码失败", e);
        }
        List<Chunk> chunkList = chunker.splitAllChunk(rootNode, fileId, language, fullContent, fileHash);
        chunkCache.put(fileHash, chunkList);
        return chunkList;
    }

    public void clearCache(Path filePath) {
        String hash = calcFileHash(filePath);
        chunkCache.remove(hash);
    }

    public void clearAllCache() {
        chunkCache.clear();
    }

    private String calcFileHash(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException(50005, "文件哈希计算失败", e);
        }
    }
}