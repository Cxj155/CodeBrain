package com.codebrain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.codebrain.config.props.CodeBrainProperties;
import com.codebrain.search.ChunkDocument;
import com.codebrain.search.dto.SearchHit;
import com.codebrain.search.dto.SearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordSearcher {
    private final ElasticsearchClient esClient;
    private final CodeBrainProperties props;

    public List<SearchHit> search(SearchRequest req) throws IOException {
        String index = props.getElasticsearch().getIndex();
        int size = Math.min(req.topK(), 50);

        MultiMatchQuery multiMatch = MultiMatchQuery.of(m -> m
                .query(req.query())
                .fields("name^5", "relativePath^3", "content^1")
        );
        Query mustQuery = Query.of(q -> q.multiMatch(multiMatch));

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolBuilder.must(mustQuery);
        if (req.repositoryId() != null) {
            boolBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("repositoryId").value(req.repositoryId())))));
        }
        if (req.language() != null && !req.language().isBlank()) {
            boolBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("language").value(req.language())))));
        }
        if (req.kind() != null && !req.kind().isBlank()) {
            boolBuilder.filter(Query.of(q -> q.term(TermQuery.of(t -> t.field("kind").value(req.kind())))));
        }


        co.elastic.clients.elasticsearch.core.SearchRequest esReq = co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
                .index(index)
                .size(size)
                .query(Query.of(q -> q.bool(boolBuilder.build())))
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
                    "keyword",
                    null
            ));
        }
        return hitList;
    }
}