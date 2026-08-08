package com.codebrain.search.fusion;

import java.util.List;

public final class RankNormalizer implements ScoreNormalizer {
    private final int rankConstant;

    public RankNormalizer(int rankConstant) {
        if (rankConstant < 1) {
            throw new IllegalArgumentException("rankConstant必须大于等于1");
        }
        this.rankConstant = rankConstant;
    }

    @Override
    public double[] normalize(List<Double> rawScores) {
        double[] result = new double[rawScores.size()];
        for (int i = 0; i < rawScores.size(); i++) {
            result[i] = 1.0 / (rankConstant + i + 1);
        }
        return result;
    }
}