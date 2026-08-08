package com.codebrain.parse;

import com.codebrain.parse.exception.ParseCrashException;
import com.codebrain.parse.exception.ParseTimeoutException;
import com.codebrain.parse.exception.ParseTooLargeException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessRunner {
    public static final int TIME_OUT_MS = 3000;
    public static final long MAX_OUTPUT_BYTE = 8 * 1024 * 1024;

    public ProcessExecResult runProcess(String[] cmd) throws ParseTimeoutException, ParseTooLargeException, ParseCrashException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(false);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new ParseCrashException("进程启动失败", e);
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> stdoutFuture = pool.submit(() -> readStream(process.getInputStream()));
        Future<String> stderrFuture = pool.submit(() -> readStream(process.getErrorStream()));

        int exitCode;
        try {
            boolean finished = process.waitFor(TIME_OUT_MS, TimeUnit.MILLISECONDS);
            exitCode = finished ? process.exitValue() : -999;
        } catch (InterruptedException e) {
            process.destroy();
            throw new ParseTimeoutException("进程等待被中断，触发超时");
        }

        if (exitCode == -999) {
            process.destroy();
            throw new ParseTimeoutException("命令执行超过3000ms超时终止");
        }

        String stdout = getFutureSafe(stdoutFuture);
        String stderr = getFutureSafe(stderrFuture);
        pool.shutdownNow();

        if (exitCode != 0) {
            throw new ParseCrashException("进程异常退出，exitCode=" + exitCode + ", stderr=" + stderr);
        }

        return new ProcessExecResult(stdout, stderr, exitCode);
    }

    private String readStream(InputStream is) throws IOException, ParseTooLargeException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        long totalSize = 0;
        while ((len = is.read(buf)) != -1) {
            totalSize += len;
            if (totalSize > MAX_OUTPUT_BYTE) {
                int allowLen = (int) (MAX_OUTPUT_BYTE - (totalSize - len));
                bos.write(buf, 0, allowLen);
                throw new ParseTooLargeException("进程输出超过8MB上限，截断终止");
            }
            bos.write(buf, 0, len);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    private String getFutureSafe(Future<String> future) {
        try {
            return future.get(TIME_OUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    public record ProcessExecResult(String stdout, String stderr, int exitCode) {}
}
