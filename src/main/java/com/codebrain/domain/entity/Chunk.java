package com.codebrain.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.codebrain.domain.enums.ChunkKind;
import lombok.Data;

@Data
@TableName("chunk")
public class Chunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private String language;
    private ChunkKind kind;
    private String name;
    private Integer startLine;
    private Integer endLine;
    private String content;
    private String contentHash;


}
