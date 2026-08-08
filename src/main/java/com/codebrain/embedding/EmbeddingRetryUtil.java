package com.codebrain.embedding;

import java.util.Random;

public class EmbeddingRetryUtil {

    private static final Random RANDOM = new Random();
    private static final long BASE_SLEEP_MS = 1000;
    private static final long MAX_SLEEP_MS = 5000;


    public static long calcBackoffJitter(int retryCount) {
        long sleep = BASE_SLEEP_MS * (1L << retryCount);
        sleep = Math.min(sleep, MAX_SLEEP_MS);
        double randomRate = 0.7 + RANDOM.nextDouble() * 0.6;
        return (long) (sleep * randomRate);
    }


    public static long parseRetryAfterMs(String retryAfterHeader) {
        if (retryAfterHeader == null || retryAfterHeader.isBlank()) {
            return 2000;
        }
        try {
            long second = Long.parseLong(retryAfterHeader);
            return second * 1000;
        } catch (NumberFormatException e) {
            return 2000;
        }
    }
}