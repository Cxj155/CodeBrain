package com.codebrain.search.fusion;

import com.codebrain.search.dto.RankedHit;
import java.util.List;

public record FusionOutput(
        List<RankedHit> ranked,
        boolean partial,
        List<String> failedSources
) {}