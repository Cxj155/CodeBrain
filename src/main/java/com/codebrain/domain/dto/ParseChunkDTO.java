package com.codebrain.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ParseChunkDTO {
    @NotBlank(message = "文件路径不能为空")
    private String filePath;

    @NotBlank(message = "编程语言不能为空")
    @Pattern(regexp = "java|python", message = "仅支持 java / python")
    private String language;
}
