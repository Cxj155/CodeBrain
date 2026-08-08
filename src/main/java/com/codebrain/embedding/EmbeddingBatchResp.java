package com.codebrain.embedding;

import lombok.Data;
import java.util.List;

@Data
public class EmbeddingBatchResp {
    private List<List<Double>> embeddings;
}