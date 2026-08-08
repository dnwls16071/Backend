package mac.resourceserver;

import mac.MacTokenPolicy;
import mac.config.MacJwtProperties;
import mac.signer.MacSecuritySigner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 직접 구현한 {@code MacTokenVerifierTest} 와 같은 시나리오를 표준 구성으로 검증한다.
 * 두 방식이 같은 판정을 내리는지 비교하는 것이 목적이다.
 */
class MacJwtDecoderConfigTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);
    private static final String ISSUER = "https://auth.example.com";
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    private final MacJwtDecoderConfig config = new MacJwtDecoderConfig();

    @Test
    @DisplayName("정상 토큰이면 주체와 권한 클레임을 그대로 돌려준다")
    void decodesValidToken() {
        String token = signer(properties(SECRET, ISSUER), NOW).sign("woojin", List.of("ROLE_USER"));

        Jwt jwt = decoderAt(NOW).decode(token);

        assertEquals("woojin", jwt.getSubject());
        assertEquals(ISSUER, jwt.getClaimAsString("iss"));
        assertEquals(List.of("ROLE_USER"), jwt.getClaimAsStringList("authorities"));
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 거부한다")
    void rejectsTokenSignedWithAnotherKey() {
        String forged = signer(properties("fedcba9876543210fedcba9876543210", ISSUER), NOW)
                .sign("woojin", List.of("ROLE_USER"));

        assertThrows(JwtException.class, () -> decoderAt(NOW).decode(forged));
    }

    @Test
    @DisplayName("신뢰하지 않는 발급자의 토큰은 서명이 유효해도 거부한다")
    void rejectsTokenFromUntrustedIssuer() {
        // 기본 validator 는 만료만 본다. JwtIssuerValidator 를 얹지 않았다면 이 토큰은 통과한다.
        String token = signer(properties(SECRET, "https://evil.example.com"), NOW)
                .sign("woojin", List.of("ROLE_USER"));

        assertThrows(JwtException.class, () -> decoderAt(NOW).decode(token));
    }

    @Test
    @DisplayName("만료 시각에 시계 오차 허용치를 더한 시점을 넘기면 거부한다")
    void rejectsExpiredToken() {
        String token = signer(properties(SECRET, ISSUER), NOW).sign("woojin", List.of("ROLE_USER"));
        Instant afterSkew = NOW.plus(TTL).plus(CLOCK_SKEW).plusSeconds(1);

        assertThrows(JwtException.class, () -> decoderAt(afterSkew).decode(token));
    }

    @Test
    @DisplayName("만료 직후라도 시계 오차 허용치 안이면 통과시킨다")
    void acceptsTokenExpiredWithinClockSkew() {
        String token = signer(properties(SECRET, ISSUER), NOW).sign("woojin", List.of("ROLE_USER"));
        Instant withinSkew = NOW.plus(TTL).plusSeconds(1);

        assertEquals("woojin", decoderAt(withinSkew).decode(token).getSubject());
    }

    @Test
    @DisplayName("정책과 다른 알고리즘으로 서명된 토큰은 거부한다")
    void rejectsTokenSignedWithUnexpectedAlgorithm() {
        // HS512 로 서명하려면 64바이트 키가 필요하다. 같은 키를 늘려 알고리즘만 바꾼다.
        MacJwtProperties hs512 = new MacJwtProperties(SECRET + SECRET, "HS512", ISSUER, TTL, CLOCK_SKEW, "authorities");
        String token = signer(hs512, NOW).sign("woojin", List.of("ROLE_USER"));

        assertThrows(JwtException.class, () -> decoderAt(NOW).decode(token));
    }

    @Test
    @DisplayName("권한 클레임 이름과 접두사가 hasRole 검사와 맞물린다")
    void convertsAuthoritiesWithoutScopePrefix() {
        MacJwtProperties properties = properties(SECRET, ISSUER);
        Jwt jwt = decoderAt(NOW).decode(signer(properties, NOW).sign("woojin", List.of("ROLE_USER")));

        var authentication = config.jwtAuthenticationConverter(properties).convert(jwt);

        assertEquals("[ROLE_USER]", authentication.getAuthorities().toString());
    }

    private JwtDecoder decoderAt(Instant instant) {
        MacJwtProperties properties = properties(SECRET, ISSUER);
        return config.jwtDecoder(properties, properties.toPolicy(), Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static MacJwtProperties properties(String secret, String issuer) {
        return new MacJwtProperties(secret, "HS256", issuer, TTL, CLOCK_SKEW, "authorities");
    }

    private static MacSecuritySigner signer(MacJwtProperties properties, Instant instant) {
        MacTokenPolicy policy = properties.toPolicy();
        return new MacSecuritySigner(properties.toKey(), policy, properties.timeToLive(),
                properties.authoritiesClaim(), Clock.fixed(instant, ZoneOffset.UTC));
    }
}
