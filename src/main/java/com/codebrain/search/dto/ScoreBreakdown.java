package com.codebrain.search.dto;

import java.util.List;

public record ScoreBreakdown(
        double keyword,
        double vector,
        double rerank,
        List<String> sources
) {}