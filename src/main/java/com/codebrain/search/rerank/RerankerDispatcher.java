package com.codebrain.search.rerank;

import com.codebrain.search.config.SearchProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class RerankerDispatcher {
    private final Map<String, Reranker> rerankMap;
    private final String activeProvider;

    public RerankerDispatcher(
            @Qualifier("noopReranker") Reranker noop,
            @Qualifier("weightedReranker") Reranker weighted,
            @Qualifier("httpReranker") Reranker http,
            SearchProperties props
    ) {
        this.rerankMap = Map.of(
                "noop", noop,
                "weighted", weighted,
                "http", http
        );
        this.activeProvider = props.rerank().provider().toLowerCase();
        if (!rerankMap.containsKey(activeProvider)) {
            throw new IllegalStateException("无效的rerank配置：" + activeProvider);
        }
    }

    public Reranker get() {
        return rerankMap.get(activeProvider);
    }

    public Map<String, Reranker> getAllRerankers() {
        return rerankMap;
    }
}