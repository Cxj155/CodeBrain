package com.codebrain.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codebrain.search")
public record SearchProperties(
        Fusion fusion,
        Rerank rerank
) {
    public record Fusion(
            String normalizer,
            Weights weights,
            int rankConstant,
            int candidateMultiplier
    ) {}

    public record Weights(double bm25, double vector, double rerank) {
        public Weights {
            double sum = bm25 + vector + rerank;
            if (Math.abs(sum - 1.0) > 1e-6) {
                throw new IllegalArgumentException("融合权重总和必须等于1.0，当前总和：" + sum);
            }
            if (bm25 < 0 || vector < 0 || rerank < 0) {
                throw new IllegalArgumentException("融合权重不能为负数");
            }
        }
    }

    public record Rerank(String provider, Http http, Weighted weighted) {}
    public record Http(String url, int timeoutMs, int maxCandidates) {}
    public record Weighted(double rerankWeight) {}
}