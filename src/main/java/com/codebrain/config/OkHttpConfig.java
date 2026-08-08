package com.codebrain.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OkHttpConfig {

    private static final int MAX_IDLE_CONNECTION = 10;
    private static final long KEEP_ALIVE_SECONDS = 30;
    private static final long CONNECT_TIMEOUT = 3000;
    private static final long READ_TIMEOUT = 8000;
    private static final long WRITE_TIMEOUT = 5000;

    @Bean
    public OkHttpClient okHttpClient() {
        ConnectionPool connectionPool = new ConnectionPool(MAX_IDLE_CONNECTION, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS);
        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }
}