package com.codebrain.parse;

import org.springframework.stereotype.Component;
import java.nio.file.Path;

@Component
public class ParserFactory {

    private final java.util.Map<String, CodeParser> parserMap;

    public ParserFactory(java.util.Map<String, CodeParser> parserMap) {
        this.parserMap = parserMap;
    }


    public CodeParser getParser(Path filePath) {
        String fileName = filePath.getFileName().toString();
        if (fileName.endsWith(".java")) {
            return parserMap.get("javaCodeParser");
        } else if (fileName.endsWith(".py")) {
            return parserMap.get("pythonCodeParser");
        }
        return parserMap.get("javaCodeParser");
    }
}