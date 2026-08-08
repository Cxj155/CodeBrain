package com.codebrain.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SNode {
    private String type;
    private String prefixText;
    private List<SNode> children = new ArrayList<>();
    private Integer startLine;
    private Integer endLine;
}
