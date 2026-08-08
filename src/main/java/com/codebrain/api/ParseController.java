package com.codebrain.api;

import com.codebrain.parse.CachingChunker;
import com.codebrain.common.Result;
import com.codebrain.domain.entity.Chunk;
import com.codebrain.parse.CodeParser;
import com.codebrain.parse.exception.ParseException;
import com.codebrain.parse.CodeParserFactory;
import com.codebrain.parse.ParseResult;
import com.codebrain.parse.ParseRetryUtil;
import com.codebrain.domain.dto.ParseChunkDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/parse")
public class ParseController {

    @Autowired
    private CodeParserFactory parserFactory;

    @Autowired
    private ParseRetryUtil retryUtil;

    @Autowired
    private CachingChunker cachingChunker;

    @GetMapping("/file")
    public Result<ParseResult> parseCodeFile(String filePath) throws ParseException {
        Path path = Path.of(filePath);
        CodeParser parser = parserFactory.getParser(path);
        ParseResult parseResult = retryUtil.parseWithRetry(parser, path);
        return Result.success(parseResult);
    }

    @GetMapping("/chunk")
    public Result<List<Chunk>> chunkFile(@Validated ParseChunkDTO dto) throws Exception {
        Path path = Path.of(dto.getFilePath());
        List<Chunk> chunkList = cachingChunker.getFileChunk(path, null, dto.getLanguage());
        return Result.success(chunkList);
    }
}
