package com.codebrain.embedding;

import lombok.Data;
import java.util.List;

@Data
public class EmbeddingResp {
    private Integer code;
    private String text;
    private List<Double> embedding;
}