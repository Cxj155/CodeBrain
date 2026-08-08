package com.codebrain.search.fusion;

import java.util.List;

public sealed interface ScoreNormalizer permits RankNormalizer, MinMaxNormalizer {
    double[] normalize(List<Double> rawScores);

    static ScoreNormalizer of(String mode) {
        return switch (mode.toLowerCase()) {
            case "minmax" -> new MinMaxNormalizer();
            default -> new RankNormalizer(60);
        };
    }
}