package com.codebrain.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codebrain")
public class CodeBrainProperties {

    private TreeSitter treeSitter = new TreeSitter();
    private Embedding embedding = new Embedding();
    private Elasticsearch elasticsearch = new Elasticsearch();

    public static class TreeSitter {
        private String executable;
        public String getExecutable() {
            return executable;
        }
        public void setExecutable(String executable) {
            this.executable = executable;
        }
    }

    public static class Embedding {
        private int batchSize = 32;
        public int getBatchSize() {
            return batchSize;
        }
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    public static class Elasticsearch {
        private String url;
        private String username;
        private String password;
        private String index;
        private Integer vectorDimension;
        private int bulkSize = 100;
        private int maxBulkRetry = 3;

        public String getUrl() {
            return url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
        public String getIndex() {
            return index;
        }
        public void setIndex(String index) {
            this.index = index;
        }
        public Integer getVectorDimension() {
            return vectorDimension;
        }
        public void setVectorDimension(Integer vectorDimension) {
            this.vectorDimension = vectorDimension;
        }
        public int getBulkSize() {
            return bulkSize;
        }
        public void setBulkSize(int bulkSize) {
            this.bulkSize = bulkSize;
        }
        public int getMaxBulkRetry() {
            return maxBulkRetry;
        }
        public void setMaxBulkRetry(int maxBulkRetry) {
            this.maxBulkRetry = maxBulkRetry;
        }
    }

    public TreeSitter getTreeSitter() {
        return treeSitter;
    }
    public void setTreeSitter(TreeSitter treeSitter) {
        this.treeSitter = treeSitter;
    }
    public Embedding getEmbedding() {
        return embedding;
    }
    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }
    public Elasticsearch getElasticsearch() {
        return elasticsearch;
    }
    public void setElasticsearch(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }
}
