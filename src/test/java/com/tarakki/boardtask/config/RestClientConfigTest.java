package com.tarakki.boardtask.config;

import com.tarakki.boardtask.client.BearerTokenForwardingInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RestClientConfigTest {

    private RestClientConfig restClientConfig;

    @BeforeEach
    void setUp() {
        restClientConfig = new RestClientConfig();
        ReflectionTestUtils.setField(restClientConfig, "connectTimeout", 3000);
        ReflectionTestUtils.setField(restClientConfig, "readTimeout", 5000);
    }

    @Test
    void restClientBuilder_shouldForwardTheCallersTokenToOrganizationService() {
        RestClient.Builder builder = restClientConfig.restClientBuilder();

        AtomicBoolean registered = new AtomicBoolean(false);
        builder.requestInterceptors(interceptors -> registered.set(
                interceptors.stream().anyMatch(BearerTokenForwardingInterceptor.class::isInstance)));

        assertTrue(registered.get());
    }
}
