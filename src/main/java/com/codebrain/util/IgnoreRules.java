package com.codebrain.util;

import org.springframework.stereotype.Component;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class IgnoreRules {

    private final List<Rule> ruleList = new ArrayList<>();

    public void loadGitIgnore(Path rootDir) throws Exception {
        ruleList.clear();
        Path gitIgnorePath = rootDir.resolve(".gitignore");
        if (!Files.exists(gitIgnorePath)) {
            return;
        }
        List<String> lines = Files.readAllLines(gitIgnorePath);
        for (String line : lines) {
            String trim = line.trim();
            if (trim.isEmpty() || trim.startsWith("#")) {
                continue;
            }
            boolean negative = trim.startsWith("!");
            String raw = negative ? trim.substring(1) : trim;
            Pattern pattern = gitPatternToRegex(raw);
            ruleList.add(new Rule(pattern, negative));
        }
    }

    public boolean isIgnored(Path filePath, Path rootDir) {
        String relative = rootDir.relativize(filePath).toString().replace("\\", "/");
        boolean ignore = false;
        for (Rule rule : ruleList) {
            if (rule.pattern.matcher(relative).matches()) {
                ignore = !rule.negative;
            }
        }
        return ignore;
    }

    private Pattern gitPatternToRegex(String rule) {
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        char[] arr = rule.toCharArray();
        int i = 0;
        while (i < arr.length) {
            char c = arr[i];
            if (c == '*') {
                if (i + 1 < arr.length && arr[i+1] == '*') {

                    sb.append(".*");
                    i += 2;
                } else {
                    sb.append("[^/]*");
                    i++;
                }
            } else if (c == '/') {
                sb.append("/");
                i++;
            } else {
                sb.append(Pattern.quote(String.valueOf(c)));
                i++;
            }
        }
        sb.append("$");
        return Pattern.compile(sb.toString());
    }

    private static class Rule {
        Pattern pattern;
        boolean negative;
        Rule(Pattern p, boolean neg) {
            pattern = p;
            negative = neg;
        }
    }
}
