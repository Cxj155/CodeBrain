package com.codebrain.search.fusion;

import com.codebrain.search.dto.SearchHit;
import com.codebrain.search.config.SearchProperties.Weights;
import java.util.List;

public record FusionInput(
        List<SearchHit> keywordHits,
        List<SearchHit> vectorHits,
        Weights weights,
        String normalizerMode
) {}