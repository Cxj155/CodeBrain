package com.codebrain.search.dto;

import java.util.List;

public record RankedHit(
        SearchHit hit,
        double keywordScore,
        double vectorScore,
        double rerankScore,
        double finalScore,
        List<String> sources
) {}