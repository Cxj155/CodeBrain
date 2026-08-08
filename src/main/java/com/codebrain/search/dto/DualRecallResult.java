package com.codebrain.search.dto;

import java.util.List;

public record DualRecallResult(
        List<SearchHit> keywordHits,
        List<SearchHit> vectorHits,
        boolean partial
) {}