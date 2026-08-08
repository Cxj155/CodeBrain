package com.codebrain.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileTagBindDTO {
    @NotNull(message = "文件ID fileId 不能为空")
    private Long fileId;

    @NotNull(message = "标签ID tagId 不能为空")
    private Long tagId;
}