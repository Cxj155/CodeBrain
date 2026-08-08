package com.codebrain.search.rerank;

import org.springframework.stereotype.Component;
import java.util.List;

@Component("noopReranker")
public class NoopReranker implements Reranker {
    @Override
    public List<RankedCandidate> rerank(String query, List<RankedCandidate> candidates) {
        return candidates;
    }

    @Override
    public String name() {
        return "noop";
    }
}