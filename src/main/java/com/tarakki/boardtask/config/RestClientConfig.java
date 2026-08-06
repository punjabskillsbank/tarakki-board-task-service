package com.tarakki.boardtask.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${restclient.connect-timeout-ms}")
    private int connectTimeout;

    @Value("${restclient.read-timeout-ms}")
    private int readTimeout;

    @Bean
    public RestClient.Builder restClientBuilder() {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
        factory.setReadTimeout(Duration.ofMillis(readTimeout));

        return RestClient.builder().requestFactory(factory);
    }

    @Bean
    public RestClient orgServiceRestClient(RestClient.Builder builder,
                                           @Value("${org.service.base-url}") String orgServiceBaseUrl) {
        return builder.baseUrl(orgServiceBaseUrl).build();
    }
}
