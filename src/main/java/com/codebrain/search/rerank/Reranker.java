package com.codebrain.search.rerank;

import java.util.List;

public interface Reranker {
    List<RankedCandidate> rerank(String query, List<RankedCandidate> candidates);
    String name();
}