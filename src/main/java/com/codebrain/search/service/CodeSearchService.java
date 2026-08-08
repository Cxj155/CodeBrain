package com.codebrain.search.service;

import com.codebrain.domain.entity.Repository;
import com.codebrain.mapper.RepositoryMapper;
import com.codebrain.search.dto.DualRecallResult;
import com.codebrain.search.dto.SearchHit;
import com.codebrain.search.dto.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeSearchService {
    private final KeywordSearcher keywordSearcher;
    private final VectorSearcher vectorSearcher;
    private final RepositoryMapper repositoryMapper;

    // 原有双路合并接口（保留兼容旧接口）
    public DualRecallResult dualRecall(SearchRequest req) {
        List<SearchHit> keywordHits = new ArrayList<>();
        List<SearchHit> vectorHits = new ArrayList<>();
        boolean partialFail = false;
        try {
            keywordHits = keywordRecall(req);
        } catch (Exception e) {
            partialFail = true;
            log.error("BM25关键词检索失败", e);
        }
        try {
            vectorHits = vectorRecall(req);
        } catch (Exception e) {
            partialFail = true;
            log.error("kNN向量检索失败", e);
        }
        return new DualRecallResult(keywordHits, vectorHits, partialFail);
    }

    // Day15新增：单独关键词召回
    public List<SearchHit> keywordRecall(SearchRequest req) throws Exception {
        return keywordSearcher.search(req);
    }

    // Day15新增：单独向量召回
    public List<SearchHit> vectorRecall(SearchRequest req) throws Exception {
        return vectorSearcher.search(req);
    }

    // 仓库存在性校验
    public void checkRepoExist(Long repoId) {
        Repository repo = repositoryMapper.selectById(repoId);
        if (repo == null) {
            throw new RuntimeException("仓库ID不存在");
        }
    }
}