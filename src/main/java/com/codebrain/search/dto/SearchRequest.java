package com.codebrain.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record SearchRequest(
        @NotBlank(message = "检索关键词query不能为空")
        @Size(max = 1000, message = "query长度不能超过1000字符")
        String query,
        Long repositoryId,
        String language,
        String kind,
        @Min(value = 1, message = "topK最小为1")
        @Max(value = 50, message = "topK最大为50")
        Integer topK
) {
    public SearchRequest {
        if (topK == null) {
            topK = 10;
        }
    }
}