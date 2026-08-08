package com.codebrain.search.rerank;

import com.codebrain.search.dto.SearchHit;
import java.util.List;

public record RankedCandidate(
        SearchHit hit,
        double keywordScore,
        double vectorScore,
        double rerankScore,
        List<String> sources
) {}