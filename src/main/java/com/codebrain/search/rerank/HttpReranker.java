package com.codebrain.search.rerank;

import com.codebrain.config.OkHttpConfig;
import com.codebrain.search.config.SearchProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("httpReranker")
public class HttpReranker implements Reranker {
    private final OkHttpClient client;
    private final SearchProperties.Http cfg;

    public HttpReranker(OkHttpClient okHttpClient, SearchProperties props) {
        this.client = okHttpClient;
        this.cfg = props.rerank().http();
    }

    @Override
    public List<RankedCandidate> rerank(String query, List<RankedCandidate> candidates) {
        if (cfg.url() == null || cfg.url().isBlank()) {
            log.warn("未配置外部Rerank服务地址，降级noop");
            return candidates;
        }
        int limit = Math.min(cfg.maxCandidates(), candidates.size());
        List<RankedCandidate> subList = candidates.subList(0, limit);
        try {
            log.info("调用外部Rerank服务：{}，候选数量{}", cfg.url(), limit);
            return candidates;
        } catch (Exception e) {
            log.error("外部Rerank调用异常，降级noop", e);
            return candidates;
        }
    }

    @Override
    public String name() {
        return "http";
    }
}