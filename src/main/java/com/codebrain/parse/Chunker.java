package com.codebrain.parse;

import com.codebrain.domain.entity.Chunk;
import com.codebrain.domain.entity.SNode;
import com.codebrain.domain.enums.ChunkKind;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Chunker {
    public List<Chunk> splitAllChunk(SNode root, Long fileId, String language, String fullFileContent, String hash) {
        List<Chunk> result = new ArrayList<>();
        AtomicInteger anonNum = new AtomicInteger(1);
        AtomicInteger lambdaNum = new AtomicInteger(1);
        traverse(root, result, fileId, language, fullFileContent, hash, anonNum, lambdaNum);
        return result;
    }

    private void traverse(SNode node, List<Chunk> list, Long fileId, String language,
                          String fileContent, String hash, AtomicInteger anonCount, AtomicInteger lambdaCount) {
        if (node == null) return;
        Chunk chunk = buildChunk(node, fileId, language, fileContent, hash, anonCount, lambdaCount);
        if (chunk != null) list.add(chunk);
        for (SNode child : node.getChildren()) traverse(child, list, fileId, language, fileContent, hash, anonCount, lambdaCount);
    }

    private Chunk buildChunk(SNode node, Long fileId, String language, String fileContent, String hash,
                             AtomicInteger anonNum, AtomicInteger lambdaNum) {
        String nodeType = node.getType();
        ChunkKind kind = null;
        String chunkName = null;
        if ("java".equals(language)) {
            switch (nodeType) {
                case "class_declaration": kind = ChunkKind.CLASS;chunkName=extractName(node,anonNum);break;
                case "interface_declaration": kind = ChunkKind.INTERFACE;chunkName=extractName(node,anonNum);break;
                case "record_declaration": kind = ChunkKind.RECORD;chunkName=extractName(node,anonNum);break;
                case "enum_declaration": kind = ChunkKind.ENUM;chunkName=extractName(node,anonNum);break;
                case "method_declaration": kind = ChunkKind.METHOD;chunkName=extractName(node,anonNum);break;
                case "constructor_declaration": kind = ChunkKind.CONSTRUCTOR;chunkName=extractName(node,anonNum);break;
            }
        } else if ("python".equals(language)) {
            switch (nodeType) {
                case "class_definition": kind = ChunkKind.CLASS;chunkName=extractName(node,anonNum);break;
                case "function_definition":
                case "async_function_definition": kind = ChunkKind.FUNCTION;chunkName=extractName(node,anonNum);break;
                case "lambda": kind = ChunkKind.LAMBDA;chunkName="$lambda$"+lambdaNum.getAndIncrement();break;
            }
        } else if ("json".equals(language)) {
            if ("object".equals(nodeType) || "array".equals(nodeType)) {
                kind = ChunkKind.CLASS;
                chunkName = "$json_root$"+anonNum.getAndIncrement();
            }
        }
        if (kind == null) return null;
        Chunk chunk = new Chunk();
        chunk.setFileId(fileId);
        chunk.setLanguage(language);
        chunk.setKind(kind);
        chunk.setName(chunkName);
        chunk.setStartLine(node.getStartLine() == null || node.getStartLine() <= 0 ? 1 : node.getStartLine());
        chunk.setEndLine(node.getEndLine() == null || node.getEndLine() <= 0 ? 1 : node.getEndLine());
        chunk.setContent(fileContent);
        chunk.setContentHash(hash);
        return chunk;
    }

    private String extractName(SNode node, AtomicInteger anonNum) {
        String idText = findIdentifierRecursive(node);
        if (idText != null && !idText.isBlank()) {
            return idText;
        }
        return "$anon$" + anonNum.getAndIncrement();
    }

    private String findIdentifierRecursive(SNode node) {
        if (node == null) return null;
        if ("identifier".equals(node.getType()) && node.getPrefixText() != null && !node.getPrefixText().isBlank()) {
            return node.getPrefixText().trim();
        }
        for (SNode child : node.getChildren()) {
            String res = findIdentifierRecursive(child);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
}