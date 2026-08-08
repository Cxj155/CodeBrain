package com.codebrain.search.rerank;

import com.codebrain.search.config.SearchProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component("weightedReranker")
public class WeightedReranker implements Reranker {
    private final double rerankWeight;

    public WeightedReranker(SearchProperties props) {
        this.rerankWeight = props.rerank().weighted().rerankWeight();
    }

    @Override
    public List<RankedCandidate> rerank(String query, List<RankedCandidate> candidates) {
        String[] qTokens = TextTokenizer.tokenize(query);
        List<RankedCandidate> result = new ArrayList<>(candidates.size());
        for (RankedCandidate candidate : candidates) {
            double overlapScore = LexicalOverlap.score(qTokens, candidate.hit().content());
            result.add(new RankedCandidate(
                    candidate.hit(),
                    candidate.keywordScore(),
                    candidate.vectorScore(),
                    overlapScore,
                    candidate.sources()
            ));
        }
        return result;
    }

    @Override
    public String name() {
        return "weighted";
    }
}

class TextTokenizer {
    public static String[] tokenize(String text) {
        return text.replaceAll("[^a-zA-Z0-9_]", " ").trim().split("\\s+");
    }
}

class LexicalOverlap {
    public static double score(String[] queryTokens, String content) {
        String[] contentTokens = TextTokenizer.tokenize(content);
        int match = 0;
        for (String q : queryTokens) {
            for (String c : contentTokens) {
                if (q.equalsIgnoreCase(c)) {
                    match++;
                    break;
                }
            }
        }
        return queryTokens.length == 0 ? 0.0 : (double) match / queryTokens.length;
    }
}