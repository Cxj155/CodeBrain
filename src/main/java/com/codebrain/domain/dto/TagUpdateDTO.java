package com.codebrain.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagUpdateDTO {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 32, message = "标签名称长度不能超过32个字符")
    private String name;

    @Size(max = 128, message = "标签描述长度不能超过128个字符")
    private String description;
}