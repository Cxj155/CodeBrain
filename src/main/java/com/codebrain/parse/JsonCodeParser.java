package com.codebrain.parse;

import com.codebrain.parse.exception.ParseException;
import com.codebrain.parse.exception.ParseSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class JsonCodeParser implements CodeParser {
    @Autowired
    private ProcessRunner processRunner;
    @Value("${codebrain.tree-sitter.executable}")
    private String treeSitterExePath;

    @Override
    public boolean supports(Path file) {
        String fileName = file.getFileName().toString();
        return fileName.endsWith(".json");
    }

    @Override
    public ParseResult parse(Path file) throws ParseException {
        long start = System.currentTimeMillis();
        String[] command = {
                treeSitterExePath,
                "parse",
                file.toAbsolutePath().toString()
        };
        ProcessRunner.ProcessExecResult execResult = processRunner.runProcess(command);
        long costMs = System.currentTimeMillis() - start;
        String stdout = execResult.stdout();
        if (stdout.contains("error") && stdout.contains("syntax")) {
            throw new ParseSyntaxException("JSON语法错误");
        }
        long nodeCount = stdout.chars().filter(c -> c == '(').count();
        return new ParseResult("json", nodeCount, costMs, ParseStatus.SUCCESS, stdout);
    }
}