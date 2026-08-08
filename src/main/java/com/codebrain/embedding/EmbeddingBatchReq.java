package com.codebrain.embedding;

import lombok.Data;
import java.util.List;

@Data
public class EmbeddingBatchReq {
    private List<String> texts;
}