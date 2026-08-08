package com.codebrain;

import com.codebrain.common.ApiLogFilter;
import com.codebrain.config.props.CodeBrainProperties;
import com.codebrain.search.config.SearchProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.codebrain.mapper")
@ConfigurationPropertiesScan
@EnableConfigurationProperties({CodeBrainProperties.class, SearchProperties.class})
public class CodeBrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeBrainApplication.class, args);
    }
    @Bean
    public FilterRegistrationBean<ApiLogFilter> apiLogFilter() {
        FilterRegistrationBean<ApiLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ApiLogFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(20);
        return bean;
    }
}