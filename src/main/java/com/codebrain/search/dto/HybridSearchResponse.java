package com.codebrain.search.dto;

import java.util.List;

public record HybridSearchResponse(
        List<RankedHit> hits,
        boolean partial,
        List<String> failedSources
) {}