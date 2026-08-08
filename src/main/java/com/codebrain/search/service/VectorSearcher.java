package com.codebrain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.codebrain.config.props.CodeBrainProperties;
import com.codebrain.embedding.EmbeddingService;
import com.codebrain.search.ChunkDocument;
import com.codebrain.search.dto.SearchHit;
import com.codebrain.search.dto.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorSearcher {
    private final ElasticsearchClient esClient;
    private final CodeBrainProperties props;
    private final EmbeddingService embeddingService;

    public List<SearchHit> search(SearchRequest req) throws Exception {
        String index = props.getElasticsearch().getIndex();
        int topK = Math.min(req.topK(), 50);
        int numCandidates = topK * 4;

        List<double[]> vecResult = embeddingService.embed(List.of(req.query()));
        if (CollectionUtils.isEmpty(vecResult)) {
            log.warn("向量服务未返回向量数据，query={}", req.query());
            return new ArrayList<>();
        }
        double[] vectorArr = vecResult.get(0);
        List<Float> queryVector = new ArrayList<>();
        for (double v : vectorArr) {
            queryVector.add((float) v);
        }

        BoolQuery.Builder filterBuilder = new BoolQuery.Builder();
        if (req.repositoryId() != null) {
            filterBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("repositoryId").value(req.repositoryId())))));
        }
        if (req.language() != null && !req.language().isBlank()) {
            filterBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("language").value(req.language())))));
        }
        if (req.kind() != null && !req.kind().isBlank()) {
            filterBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("kind").value(req.kind())))));
        }
        Query filterQuery = Query.of(q -> q.bool(filterBuilder.build()));

        co.elastic.clients.elasticsearch.core.SearchRequest esReq = co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
                .index(index)
                .size(topK)
                .knn(k -> k
                        .field("embedding")
                        .queryVector(queryVector)
                        .k((long) topK)
                        .numCandidates((long) numCandidates)
                        .filter(filterQuery) // 注入统一过滤条件
                )
        );

        SearchResponse<ChunkDocument> resp = esClient.search(esReq, ChunkDocument.class);
        List<SearchHit> hitList = new ArrayList<>();
        int rank = 1;
        for (Hit<ChunkDocument> hit : resp.hits().hits()) {
            ChunkDocument doc = hit.source();
            if (doc == null) continue;
            hitList.add(new SearchHit(
                    hit.id(),
                    doc.chunkId(),
                    doc.fileId(),
                    doc.relativePath(),
                    doc.language(),
                    doc.kind(),
                    doc.name(),
                    doc.content(),
                    doc.startLine(),
                    doc.endLine(),
                    hit.score() != null ? hit.score() : 0.0,
                    rank++,
                    "vector",
                    null
            ));
        }
        return hitList;
    }
}