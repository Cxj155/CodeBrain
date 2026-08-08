package com.codebrain.search.service;

import com.codebrain.common.BizException;
import com.codebrain.common.BusinessException;
import com.codebrain.embedding.exception.EmbeddingException;
import com.codebrain.search.config.SearchProperties;
import com.codebrain.search.dto.*;
import com.codebrain.search.fusion.FusionInput;
import com.codebrain.search.fusion.FusionOutput;
import com.codebrain.search.fusion.WeightedFusionService;
import com.codebrain.search.rerank.RankedCandidate;
import com.codebrain.search.rerank.Reranker;
import com.codebrain.search.rerank.RerankerDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class HybridSearchService {
    private final CodeSearchService codeSearchService;
    private final WeightedFusionService fusionService;
    private final RerankerDispatcher rerankerDispatcher;
    private final SearchProperties searchProps;

    public HybridSearchService(CodeSearchService codeSearchService,
                               WeightedFusionService fusionService,
                               RerankerDispatcher rerankerDispatcher,
                               SearchProperties searchProperties) {
        this.codeSearchService = codeSearchService;
        this.fusionService = fusionService;
        this.rerankerDispatcher = rerankerDispatcher;
        this.searchProps = searchProperties;
    }

    public HybridSearchResponse search(SearchRequest req) {
        // 校验仓库存在性（复用原有逻辑）
        if (req.repositoryId() != null) {
            codeSearchService.checkRepoExist(req.repositoryId());
        }

        List<String> failedSources = new ArrayList<>();
        List<SearchHit> keywordHits = List.of();
        List<SearchHit> vectorHits = List.of();

        // 1.向量检索
        try {
            vectorHits = codeSearchService.vectorRecall(req);
        } catch (Exception e) {
            failedSources.add("EMBEDDING");
            log.warn("向量召回失败: {}", e.getMessage(), e);
        }

        // 2.BM25关键词检索
        try {
            keywordHits = codeSearchService.keywordRecall(req);
        } catch (Exception e) {
            failedSources.add("BM25");
            log.warn("关键词召回失败：{}", e.getMessage());
        }

        // 两路全部失败抛出业务异常
        if (keywordHits.isEmpty() && vectorHits.isEmpty()) {
            throw new BizException(50010, "关键词、向量两路检索全部失败，无检索结果");
        }

        // 3.加权融合
        FusionInput fusionInput = new FusionInput(keywordHits, vectorHits, null, null);
        FusionOutput fusionOutput = fusionService.fuse(fusionInput);

        // 4.转换候选并执行rerank
        List<RankedCandidate> candidates = fusionOutput.ranked().stream()
                .map(h -> new RankedCandidate(h.hit(), h.keywordScore(), h.vectorScore(), h.rerankScore(), h.sources()))
                .toList();
        Reranker activeReranker = rerankerDispatcher.get();
        List<RankedCandidate> rerankedCandidates = activeReranker.rerank(req.query(), candidates);

        // 5.重新计算总分并截断topK
        SearchProperties.Weights weights = searchProps.fusion().weights();
        List<RankedHit> finalResult = rerankedCandidates.stream()
                .map(rc -> new RankedHit(
                        rc.hit(),
                        rc.keywordScore(),
                        rc.vectorScore(),
                        rc.rerankScore(),
                        weights.bm25() * rc.keywordScore()
                                + weights.vector() * rc.vectorScore()
                                + weights.rerank() * rc.rerankScore(),
                        rc.sources()
                ))
                .sorted((a, b) -> Double.compare(b.finalScore(), a.finalScore()))
                .limit(req.topK())
                .toList();

        // 标记部分失败
        boolean partial = !failedSources.isEmpty() || !activeReranker.name().equals("noop");
        return new HybridSearchResponse(finalResult, partial, failedSources);
    }
}