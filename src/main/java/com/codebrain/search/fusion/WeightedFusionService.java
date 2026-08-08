package com.codebrain.search.fusion;

import com.codebrain.search.config.SearchProperties;
import com.codebrain.search.dto.RankedHit;
import com.codebrain.search.dto.SearchHit;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WeightedFusionService {
    private final SearchProperties props;

    public WeightedFusionService(SearchProperties props) {
        this.props = props;
    }

    public FusionOutput fuse(FusionInput input) {
        SearchProperties.Weights weights = input.weights() != null
                ? input.weights()
                : props.fusion().weights();
        ScoreNormalizer normalizer = ScoreNormalizer.of(
                input.normalizerMode() != null
                        ? input.normalizerMode()
                        : props.fusion().normalizer());

        double[] kwNorm = normalizer.normalize(
                input.keywordHits().stream().map(SearchHit::rawScore).toList());
        double[] vecNorm = normalizer.normalize(
                input.vectorHits().stream().map(SearchHit::rawScore).toList());

        Map<String, Aggregated> aggMap = new LinkedHashMap<>();
        for (int i = 0; i < input.keywordHits().size(); i++) {
            SearchHit hit = input.keywordHits().get(i);
            aggMap.computeIfAbsent(hit.id(), k -> new Aggregated(hit)).tapKeyword(kwNorm[i]);
        }
        for (int i = 0; i < input.vectorHits().size(); i++) {
            SearchHit hit = input.vectorHits().get(i);
            aggMap.computeIfAbsent(hit.id(), k -> new Aggregated(hit)).tapVector(vecNorm[i]);
        }

        List<RankedHit> rankedList = new ArrayList<>();
        for (Aggregated agg : aggMap.values()) {
            double finalScore = weights.bm25() * agg.keywordScore
                    + weights.vector() * agg.vectorScore
                    + weights.rerank() * 0.0;
            rankedList.add(new RankedHit(
                    agg.hit,
                    agg.keywordScore,
                    agg.vectorScore,
                    0.0,
                    finalScore,
                    agg.sources
            ));
        }

        rankedList.sort(Comparator
                .comparingDouble(RankedHit::finalScore).reversed()
                .thenComparing(r -> r.hit().id()));

        int maxCandidate = props.fusion().candidateMultiplier() * 50;
        if (rankedList.size() > maxCandidate) {
            rankedList = rankedList.subList(0, maxCandidate);
        }
        return new FusionOutput(rankedList, false, List.of());
    }

    private static final class Aggregated {
        final SearchHit hit;
        double keywordScore = 0.0;
        double vectorScore = 0.0;
        final List<String> sources = new ArrayList<>(2);

        Aggregated(SearchHit hit) {
            this.hit = hit;
        }

        Aggregated tapKeyword(double score) {
            this.keywordScore = score;
            if (!sources.contains("BM25")) sources.add("BM25");
            return this;
        }

        Aggregated tapVector(double score) {
            this.vectorScore = score;
            if (!sources.contains("VECTOR")) sources.add("VECTOR");
            return this;
        }
    }
}