package com.codebrain.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.codebrain.config.props.CodeBrainProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CodeBrainProperties.class)
public class ElasticsearchConfig {
    @Bean
    public ElasticsearchClient elasticsearchClient(CodeBrainProperties properties) {
        CodeBrainProperties.Elasticsearch esProp = properties.getElasticsearch();
        String url = esProp.getUrl();
        String cleanUrl = url.replace("http://", "").replace("https://", "");
        String[] hostPort = cleanUrl.split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient,
                new co.elastic.clients.json.jackson.JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    public DisposableBean closeEsTransport(ElasticsearchClient client) {
        return () -> {
            client._transport().close();
        };
    }
}