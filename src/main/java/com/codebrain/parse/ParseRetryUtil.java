package com.codebrain.parse;

import com.codebrain.parse.exception.ParseException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class ParseRetryUtil {

    private static final int MAX_RETRY_TIMES = 3;
    private static final long[] BACKOFF_MS = {200, 600, 1500};

    public ParseResult parseWithRetry(CodeParser parser, Path file) throws ParseException {
        ParseException lastException = null;
        for (int attempt = 0; attempt <= MAX_RETRY_TIMES; attempt++) {
            try {
                return parser.parse(file);
            } catch (ParseException e) {
                lastException = e;
                if (!e.isRetryable()) {
                    throw e;
                }
                if (attempt >= MAX_RETRY_TIMES) {
                    break;
                }
                long sleepTime = BACKOFF_MS[attempt];
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw lastException;
    }
}
