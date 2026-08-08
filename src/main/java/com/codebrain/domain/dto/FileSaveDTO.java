package com.codebrain.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FileSaveDTO {
    @NotNull(message = "仓库ID repositoryId 不能为空")
    private Long repositoryId;

    @NotBlank(message = "文件路径 path 不能为空")
    @Size(max = 256, message = "文件路径长度不能超过256字符")
    private String path;

    @NotBlank(message = "sha256哈希值不能为空")
    private String sha256;

    @NotNull(message = "修改时间 mtime 不能为空")
    private Long mtime;

    @NotBlank(message = "编程语言 language 不能为空")
    private String language;

    @NotNull(message = "文件大小 sizeBytes 不能为空")
    private Long sizeBytes;
}