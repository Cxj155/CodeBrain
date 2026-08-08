package com.codebrain.parse;

import com.codebrain.parse.exception.UnsupportedLanguageException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class CodeParserFactory {
    public CodeParserFactory(JavaCodeParser javaCodeParser,
                             PythonCodeParser pythonCodeParser,
                             JsonCodeParser jsonCodeParser) {
        parserList = List.of(javaCodeParser, pythonCodeParser, jsonCodeParser);
    }
    private final List<CodeParser> parserList;

    public CodeParser getParser(Path file) throws UnsupportedLanguageException {
        for (CodeParser parser : parserList) {
            if (parser.supports(file)) {
                return parser;
            }
        }
        throw new UnsupportedLanguageException("不支持解析该文件：" + file.getFileName());
    }

    public String parseFile(Path file) throws Exception {
        CodeParser parser = getParser(file);
        return parser.parse(file).stdout();
    }

    public String detectLanguage(Path filePath) {
        String name = filePath.getFileName().toString();
        if (name.endsWith(".java")) return "java";
        if (name.endsWith(".py")) return "python";
        if (name.endsWith(".json")) return "json";
        return "unknown";
    }
}