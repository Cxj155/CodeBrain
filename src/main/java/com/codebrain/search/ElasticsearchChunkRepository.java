package com.codebrain.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.codebrain.config.props.CodeBrainProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ElasticsearchChunkRepository {
    private final ElasticsearchClient esClient;
    private final CodeBrainProperties properties;

    private String getIndexName() {
        return properties.getElasticsearch().getIndex();
    }

    @PostConstruct
    public void initIndex() throws IOException {
        ExistsRequest existsRequest = ExistsRequest.of(r -> r.index(getIndexName()));
        boolean exists = esClient.indices().exists(existsRequest).value();
        if (exists) {
            return;
        }
        int dims = properties.getElasticsearch().getVectorDimension();
        String mappingJson = "{" +
                "\"settings\":{\"number_of_shards\":1,\"number_of_replicas\":1}," +
                "\"mappings\":{" +
                "\"properties\":{" +
                "\"chunkId\":{\"type\":\"long\"}," +
                "\"fileId\":{\"type\":\"long\"}," +
                "\"repositoryId\":{\"type\":\"long\"}," +
                "\"relativePath\":{\"type\":\"keyword\"}," +
                "\"language\":{\"type\":\"keyword\"}," +
                "\"kind\":{\"type\":\"keyword\"}," +
                "\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}," +
                "\"content\":{\"type\":\"text\"}," +
                "\"startLine\":{\"type\":\"integer\"}," +
                "\"endLine\":{\"type\":\"integer\"}," +
                "\"contentHash\":{\"type\":\"keyword\"}," +
                "\"embedding\":{\"type\":\"dense_vector\",\"dims\":" + dims + ",\"index\":true,\"similarity\":\"cosine\"}" +
                "}}}";
        CreateIndexRequest createReq = CreateIndexRequest.of(b -> b
                .index(getIndexName())
                .withJson(new StringReader(mappingJson))
        );
        esClient.indices().create(createReq);
        log.info("ES向量索引初始化完成:{}", getIndexName());
    }

    public void save(ChunkDocument doc) throws IOException {
        IndexRequest<ChunkDocument> request = IndexRequest.of(i -> i
                .index(getIndexName())
                .id(doc.chunkId().toString())
                .document(doc)
        );
        esClient.index(request);
    }

    public void bulkUpsert(List<ChunkDocument> docList) throws IOException {
        if (docList.isEmpty()) return;
        List<BulkOperation> ops = new ArrayList<>();
        for (ChunkDocument doc : docList) {
            IndexOperation<ChunkDocument> op = IndexOperation.of(i -> i
                    .index(getIndexName())
                    .id(doc.chunkId().toString())
                    .document(doc)
            );
            ops.add(BulkOperation.of(o -> o.index(op)));
        }
        BulkRequest bulkReq = BulkRequest.of(r -> r.operations(ops));
        BulkResponse bulkResp = esClient.bulk(bulkReq);
        if (bulkResp.errors()) {
            log.error("Bulk写入ES存在失败文档，文档数量:{}", docList.size());
            bulkResp.items().forEach(item -> {
                if (item.error() != null) {
                    log.error("ES文档写入失败 chunkId={}, error={}", item.id(), item.error().reason());
                }
            });
            throw new IOException("Bulk写入ES存在失败文档");
        }
    }

    public void deleteByFileId(Long fileId) throws IOException {
        DeleteByQueryRequest delReq = DeleteByQueryRequest.of(r -> r
                .index(getIndexName())
                .query(q -> q.term(t -> t.field("fileId").value(fileId)))
        );
        DeleteByQueryResponse resp = esClient.deleteByQuery(delReq);
        log.info("删除fileId={} ES分片文档数量:{}", fileId, resp.deleted());
    }

    public List<ChunkDocument> searchByVector(List<Float> vector, int topN) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
                .index(getIndexName())
                .size(topN)
                .knn(k -> k
                        .field("embedding")
                        .queryVector(vector)
                        .k((long) topN)
                )
        );
        SearchResponse<ChunkDocument> response = esClient.search(request, ChunkDocument.class);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public long count() throws IOException {
        CountRequest countRequest = CountRequest.of(c -> c.index(getIndexName()));
        CountResponse countResponse = esClient.count(countRequest);
        return countResponse.count();
    }

    public List<ChunkDocument> searchByText(String keyword, int topN) throws IOException {
        SearchRequest request = SearchRequest.of(s -> s
                .index(getIndexName())
                .size(topN)
                .query(q -> q.match(m -> m.field("content").query(keyword)))
        );
        SearchResponse<ChunkDocument> response = esClient.search(request, ChunkDocument.class);
        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public void delete(Long chunkId) throws IOException {
        DeleteRequest deleteRequest = DeleteRequest.of(d -> d
                .index(getIndexName())
                .id(chunkId.toString())
        );
        esClient.delete(deleteRequest);
    }
}