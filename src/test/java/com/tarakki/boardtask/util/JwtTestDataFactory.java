package com.tarakki.boardtask.util;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class JwtTestDataFactory {

    public static final String SECRET = "test-jwt-secret-key-must-be-at-least-32-bytes-long";
    public static final String OTHER_SECRET = "another-jwt-secret-key-that-is-at-least-32-bytes-long";

    public static String createValidToken(UUID memberId) {
        Instant now = Instant.now();
        return encode(SECRET, memberId, now, now.plus(Duration.ofHours(1)));
    }

    /** A correctly signed, unexpired token whose subject is not a member id. */
    public static String createValidTokenWithSubject(String subject) {
        Instant now = Instant.now();
        return encode(SECRET, subject, now, now.plus(Duration.ofHours(1)));
    }

    public static String createExpiredToken(UUID memberId) {
        Instant now = Instant.now();
        return encode(SECRET, memberId, now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)));
    }

    public static String createTokenSignedWithOtherSecret(UUID memberId) {
        Instant now = Instant.now();
        return encode(OTHER_SECRET, memberId, now, now.plus(Duration.ofHours(1)));
    }

    /**
     * Keeps the signature of a genuine token for {@code realMemberId} but swaps in the payload of a token
     * for {@code forgedMemberId}, so the signature no longer matches the content.
     */
    public static String createTamperedToken(UUID realMemberId, UUID forgedMemberId) {
        String[] real = createValidToken(realMemberId).split("\\.");
        String[] forged = createValidToken(forgedMemberId).split("\\.");
        return real[0] + "." + forged[1] + "." + real[2];
    }

    private static String encode(String secret, UUID memberId, Instant issuedAt, Instant expiresAt) {
        return encode(secret, memberId.toString(), issuedAt, expiresAt);
    }

    private static String encode(String secret, String subject, Instant issuedAt, Instant expiresAt) {
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
