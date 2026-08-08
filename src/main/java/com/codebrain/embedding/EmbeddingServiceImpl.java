package com.codebrain.embedding;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.codebrain.embedding.exception.EmbeddingException;
import com.codebrain.embedding.exception.EmbeddingTransientException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient okHttpClient;
    @Value("${embedding.url}")
    private String remoteEmbeddingUrl;
    @Value("${embedding.model:bge-small-zh}")
    private String embeddingModel;
    @Value("${embedding.retry.max-retries:3}")
    private int maxRetryTimes;
    @Value("${embedding.retry.total-budget-ms:10000}")
    private long totalBudgetMs;

    public EmbeddingServiceImpl(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public List<double[]> embed(List<String> texts) {
        if (CollectionUtils.isEmpty(texts)) {
            throw new IllegalArgumentException("待向量化文本列表不可为空");
        }
        long startTs = System.currentTimeMillis();
        int currentRetry = 0;
        Exception finalException = null;
        while (currentRetry <= maxRetryTimes) {
            if (System.currentTimeMillis() - startTs > totalBudgetMs) {
                break;
            }
            EmbeddingRequest requestBody = new EmbeddingRequest();
            requestBody.model = embeddingModel;
            requestBody.input = texts;
            String reqJson = JSON.toJSONString(requestBody);
            RequestBody body = RequestBody.create(reqJson, JSON_MEDIA);
            Request httpReq = new Request.Builder()
                    .url(remoteEmbeddingUrl)
                    .post(body)
                    .build();
            try (Response response = okHttpClient.newCall(httpReq).execute()) {
                if (response.code() == 429) {
                    String retryAfter = response.header("Retry-After");
                    long waitMs = EmbeddingRetryUtil.parseRetryAfterMs(retryAfter);
                    log.warn("触发限流429，按Retry‑After休眠{}ms", waitMs);
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new EmbeddingTransientException("限流等待被中断", ie);
                    }
                    currentRetry++;
                    continue;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("HTTP请求异常，状态码：" + response.code());
                }
                String respStr = response.body().string();
                EmbeddingResponse resp;
                try {
                    resp = JSON.parseObject(respStr, EmbeddingResponse.class);
                } catch (JSONException e) {
                    log.error("向量接口返回JSON解析失败，response={}", respStr, e);
                    throw new EmbeddingException("向量返回报文解析失败，不重试", e);
                }

                boolean successFlag = resp.isSuccess();
                if (!successFlag) {
                    log.error("向量服务业务返回失败，msg={}", resp.getMsg());
                    throw new EmbeddingException("向量服务业务异常：" + resp.getMsg());
                }


                List<double[]> embeddingList = new ArrayList<>();
                List<EmbeddingItem> itemList = resp.getData();
                if (itemList != null && !itemList.isEmpty()) {

                    if (itemList != null && !itemList.isEmpty()) {
                        for (EmbeddingItem item : itemList) {
                            double[] vec = item.getEmbedding();
                            double[] fullVec = new double[1536];
                            System.arraycopy(vec, 0, fullVec, 0, vec.length);
                            log.info("原始向量长度:{}, 补齐后:1536", vec.length);
                            embeddingList.add(fullVec);
                        }
                    }
                }
                return embeddingList;

            } catch (EmbeddingException e) {
                throw e;
            } catch (Exception e) {
                finalException = e;
                currentRetry++;
                if (currentRetry > maxRetryTimes) {
                    break;
                }
                long sleepTime = EmbeddingRetryUtil.calcBackoffJitter(currentRetry);
                log.warn("向量化调用失败，第{}次重试，休眠{}ms，异常原因：{}", currentRetry, sleepTime, e.getMessage());
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new EmbeddingTransientException("重试休眠被中断", ie);
                }
            }
        }
        log.error("向量化重试{}次全部失败", maxRetryTimes, finalException);
        throw new EmbeddingTransientException("远程向量接口重试耗尽失败", finalException);
    }

    public static class EmbeddingRequest {
        public String model;
        public List<String> input;
    }

    public static class EmbeddingItem {
        private Integer index;
        private double[] embedding;

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public double[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(double[] embedding) {
            this.embedding = embedding;
        }
    }

    public static class EmbeddingResponse {
        private String object;
        private String model;
        private List<EmbeddingItem> data;
        private boolean success = true;
        private String msg = "";

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<EmbeddingItem> getData() {
            return data;
        }

        public void setData(List<EmbeddingItem> data) {
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}