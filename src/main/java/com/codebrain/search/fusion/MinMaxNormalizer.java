package com.codebrain.search.fusion;

import java.util.List;

public final class MinMaxNormalizer implements ScoreNormalizer {
    @Override
    public double[] normalize(List<Double> rawScores) {
        if (rawScores.isEmpty()) {
            return new double[0];
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Double score : rawScores) {
            min = Math.min(min, score);
            max = Math.max(max, score);
        }
        double span = max - min;
        double[] result = new double[rawScores.size()];
        if (span == 0.0) {
            java.util.Arrays.fill(result, 1.0);
            return result;
        }
        for (int i = 0; i < rawScores.size(); i++) {
            result[i] = (rawScores.get(i) - min) / span;
        }
        return result;
    }
}