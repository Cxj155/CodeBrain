package com.codebrain.parse;

import com.codebrain.common.BusinessException;
import com.codebrain.domain.entity.SNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SExpressionParser {
    private static final int MAX_RECURSIVE_DEPTH = 1000;
    private static final int MAX_SCAN_POS = 1000000;
    private int pos;
    private char[] chars;

    public SNode parse(String sExpr) {
        String clean = preHandle(sExpr);
        this.chars = clean.toCharArray();
        this.pos = 0;
        try {
            return readNode(0);
        } catch (Exception e) {
            log.error("S表达式解析失败", e);
            return null;
        }
    }

    private String preHandle(String input) {
        StringBuilder sb = new StringBuilder();
        String[] lines = input.split("\n");
        for (String line : lines) {
            int commentIdx = line.indexOf(';');
            if (commentIdx != -1) line = line.substring(0, commentIdx);
            sb.append(line.replaceAll("\\s+", " "));
        }
        return sb.toString().trim();
    }

    private SNode readNode(int depth) {
        if (depth > MAX_RECURSIVE_DEPTH) {
            log.error("AST解析递归深度超过阈值{}，存在异常嵌套结构", MAX_RECURSIVE_DEPTH);
            throw new BusinessException(50004, "代码AST嵌套过深，解析终止");
        }
        if (pos > MAX_SCAN_POS) {
            throw new BusinessException(50005, "解析扫描位置超限，终止解析");
        }

        skipBlank();
        if (pos >= chars.length || chars[pos] != '(') {
            return null;
        }
        pos++;

        SNode node = new SNode();
        String token = readToken();
        int colonIndex = token.indexOf(':');
        if (colonIndex > 0) {
            node.setPrefixText(token.substring(0, colonIndex + 1));
            node.setType(token.substring(colonIndex + 1).trim());
        } else {
            node.setPrefixText("");
            node.setType(token);
        }

        List<SNode> children = new ArrayList<>();
        while (true) {
            skipBlank();
            if (pos >= chars.length || chars[pos] == ')') {
                pos++;
                break;
            }
            SNode child = readNode(depth + 1);
            if (child != null) {
                children.add(child);
            } else {
                pos++;
            }
        }
        node.setChildren(children);
        return node;
    }

    private String readToken() {
        skipBlank();
        int start = pos;
        while (pos < chars.length && chars[pos] != ' ' && chars[pos] != '(' && chars[pos] != ')') {
            pos++;
        }
        return new String(chars, start, pos - start);
    }

    private void skipBlank() {
        while (pos < chars.length && Character.isWhitespace(chars[pos])) {
            pos++;
        }
    }
}