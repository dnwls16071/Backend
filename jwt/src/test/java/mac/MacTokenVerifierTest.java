package mac;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import mac.custom.MacTokenVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import mac.signer.MacSecuritySigner;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MacTokenVerifierTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);
    private static final String ISSUER = "https://auth.example.com";
    private static final String AUTHORITIES_CLAIM = "authorities";

    // HS512 까지 쓸 수 있도록 64바이트. 알고리즘 혼동(alg confusion) 검증에 필요하다.
    private static final OctetSequenceKey KEY = key("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
    private static final OctetSequenceKey OTHER_KEY = key("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");

    private final MacTokenPolicy policy = new MacTokenPolicy(JWSAlgorithm.HS256, ISSUER, CLOCK_SKEW);

    @Test
    @DisplayName("정상 토큰이면 주체와 권한 클레임을 그대로 돌려준다")
    void verifiesValidToken() {
        String token = signerWith(KEY, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        JWTClaimsSet claims = verifierWith(KEY, policy, clockAt(NOW)).verify(token);

        assertEquals("woojin", claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(List.of("ROLE_USER"), claims.getClaim(AUTHORITIES_CLAIM));
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 서명 불일치로 거부한다")
    void rejectsTokenSignedWithAnotherKey() {
        String forged = signerWith(OTHER_KEY, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(NOW)).verify(forged));
    }

    @Test
    @DisplayName("정책과 다른 알고리즘으로 서명된 토큰은 서명이 유효해도 거부한다")
    void rejectsTokenSignedWithUnexpectedAlgorithm() {
        // 같은 키·같은 발급자지만 alg 만 HS512. 헤더의 alg 를 믿으면 통과해버리는 상황이다.
        MacTokenPolicy hs512 = new MacTokenPolicy(JWSAlgorithm.HS512, ISSUER, CLOCK_SKEW);
        String token = signerWith(KEY, hs512, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(NOW)).verify(token));
    }

    @Test
    @DisplayName("신뢰하지 않는 발급자의 토큰은 거부한다")
    void rejectsTokenFromUntrustedIssuer() {
        MacTokenPolicy otherIssuer = new MacTokenPolicy(JWSAlgorithm.HS256, "https://evil.example.com", CLOCK_SKEW);
        String token = signerWith(KEY, otherIssuer, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(NOW)).verify(token));
    }

    @Test
    @DisplayName("만료 시각에 시계 오차 허용치를 더한 시점을 넘기면 거부한다")
    void rejectsExpiredToken() {
        String token = signerWith(KEY, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));
        Instant afterSkew = NOW.plus(TTL).plus(CLOCK_SKEW).plusSeconds(1);

        assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(afterSkew)).verify(token));
    }

    @Test
    @DisplayName("만료 직후라도 시계 오차 허용치 안이면 통과시킨다")
    void acceptsTokenExpiredWithinClockSkew() {
        String token = signerWith(KEY, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));
        Instant withinSkew = NOW.plus(TTL).plusSeconds(1);

        JWTClaimsSet claims = verifierWith(KEY, policy, clockAt(withinSkew)).verify(token);

        assertEquals("woojin", claims.getSubject());
    }

    @Test
    @DisplayName("JWT 형식이 아닌 문자열은 파싱 단계에서 거부한다")
    void rejectsMalformedToken() {
        InvalidBearerTokenException e = assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(NOW)).verify("not-a-jwt"));

        assertInstanceOf(ParseException.class, e.getCause());
    }

    @Test
    @DisplayName("실패 사유는 401/invalid_token 으로만 드러나고, 한글 사유는 응답에 실리지 않는다")
    void doesNotExposeKoreanFailureReason() {
        // RFC 6750 은 error_description 에 ASCII 일부만 허용한다. 한글 사유를 넘기면
        // BearerTokenErrors 가 IllegalArgumentException 을 삼키고 "Invalid token" 으로 갈아끼운다.
        // 즉 MacTokenVerifier 가 넘긴 한글 사유는 응답에도 로그에도 남지 않는다.
        InvalidBearerTokenException e = assertThrows(InvalidBearerTokenException.class,
                () -> verifierWith(KEY, policy, clockAt(NOW)).verify(
                        signerWith(KEY, policy, clockAt(NOW.minus(TTL).minus(CLOCK_SKEW).minusSeconds(1)))
                                .sign("woojin", List.of("ROLE_USER"))));

        BearerTokenError error = (BearerTokenError) e.getError();
        assertEquals("invalid_token", error.getErrorCode());
        assertEquals(HttpStatus.UNAUTHORIZED, error.getHttpStatus());
        assertEquals("Invalid token", error.getDescription());
    }

    static MacSecuritySigner signerWith(OctetSequenceKey key, MacTokenPolicy policy, Clock clock) {
        return new MacSecuritySigner(key, policy, TTL, AUTHORITIES_CLAIM, clock);
    }

    static MacTokenVerifier verifierWith(OctetSequenceKey key, MacTokenPolicy policy, Clock clock) {
        return new MacTokenVerifier(key, policy, clock);
    }

    static Clock clockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    private static OctetSequenceKey key(String secret) {
        return new OctetSequenceKey.Builder(secret.getBytes(StandardCharsets.UTF_8)).build();
    }
}
