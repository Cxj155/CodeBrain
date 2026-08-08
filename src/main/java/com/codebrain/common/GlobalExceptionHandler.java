package com.codebrain.common;
import com.codebrain.embedding.exception.*;
import com.codebrain.parse.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError.getDefaultMessage();
        return Result.build(40001, message, null);
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.build(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(ParseTimeoutException.class)
    public Result<Void> handleParseTimeout(ParseTimeoutException e) {
        return Result.build(41001, "解析超时：" + e.getMessage(), null);
    }
    @ExceptionHandler(ParseCrashException.class)
    public Result<Void> handleParseCrash(ParseCrashException e) {
        return Result.build(41002, "解析进程崩溃：" + e.getMessage(), null);
    }
    @ExceptionHandler(ParseSyntaxException.class)
    public Result<Void> handleSyntaxError(ParseSyntaxException e) {
        return Result.build(41003, "代码语法错误：" + e.getMessage(), null);
    }
    @ExceptionHandler(ParseTooLargeException.class)
    public Result<Void> handleTooLarge(ParseTooLargeException e) {
        return Result.build(41004, "文件超出8MB限制：" + e.getMessage(), null);
    }
    @ExceptionHandler(UnsupportedLanguageException.class)
    public Result<Void> handleUnsupported(UnsupportedLanguageException e) {
        return Result.build(41005, "不支持的文件类型：" + e.getMessage(), null);
    }

    @ExceptionHandler(EmbeddingException.class)
    public Result<Void> handleEmbeddingErr(EmbeddingException e) {
        return Result.build(51001, "向量服务异常：" + e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleGlobalException(Exception e) {
        e.printStackTrace();
        return Result.build(500, "服务器内部异常", null);
    }

    @ExceptionHandler(IOException.class)
    public Result<Void> handleEsIoException(IOException e) {
        log.error("ES通信超时/IO异常", e);
        return Result.build(42001, "Elasticsearch服务通信异常", null);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleSearchRuntime(RuntimeException e) {
        String msg = e.getMessage();
        if ("仓库ID不存在".equals(msg)) {
            return Result.build(40401, "指定仓库不存在", null);
        }
        return Result.build(500, msg, null);
    }

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.build(e.getCode(), e.getMessage(), null);
    }
}