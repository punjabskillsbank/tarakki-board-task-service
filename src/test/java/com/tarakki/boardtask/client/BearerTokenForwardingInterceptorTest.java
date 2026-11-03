package com.tarakki.boardtask.client;

import com.tarakki.boardtask.util.JwtTestDataFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BearerTokenForwardingInterceptorTest {

    private static final String URL = "http://org-service/api/organizations/1/members";

    private MockRestServiceServer server;
    private RestClient restClient;
    private String tokenValue;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .requestInterceptor(new BearerTokenForwardingInterceptor());
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();

        tokenValue = JwtTestDataFactory.createValidToken(UUID.randomUUID());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldForwardTheCallersTokenWhenAuthenticatedWithAJwt() {
        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        server.expect(requestTo(URL))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        restClient.get().uri(URL).retrieve().toBodilessEntity();

        server.verify();
    }

    @Test
    void shouldSendNoAuthorizationHeaderWhenNobodyIsAuthenticated() {
        server.expect(requestTo(URL))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        restClient.get().uri(URL).retrieve().toBodilessEntity();

        server.verify();
    }

    @Test
    void shouldNotForwardCredentialsThatAreNotAJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("someone", "secret", List.of()));
        server.expect(requestTo(URL))
                .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        restClient.get().uri(URL).retrieve().toBodilessEntity();

        server.verify();
    }
}
