package com.codebrain.embedding;

import java.util.List;

public interface EmbeddingService {
    List<double[]> embed(List<String> texts);
}