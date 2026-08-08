package com.codebrain.parse;

import com.codebrain.parse.exception.ParseException;

import java.nio.file.Path;

public interface CodeParser {
    boolean supports(Path file);
    ParseResult parse(Path file) throws ParseException;
}
