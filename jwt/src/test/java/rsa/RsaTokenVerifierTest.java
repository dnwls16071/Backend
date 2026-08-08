package rsa;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import mac.MacTokenPolicy;
import mac.signer.MacSecuritySigner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import rsa.custom.RsaTokenVerifier;
import rsa.signer.RsaSecuritySigner;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MacTokenVerifierTest 와 같은 시나리오를 비대칭키로 돌린다.
 * 두 방식의 판정이 일치해야 서로 바꿔 끼울 수 있다는 뜻이다.
 */
class RsaTokenVerifierTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);
    private static final String ISSUER = "https://auth.example.com";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private static RSAKey key;
    private static RSAKey otherKey;

    private final RsaTokenPolicy policy = new RsaTokenPolicy(JWSAlgorithm.RS256, ISSUER, CLOCK_SKEW);

    @BeforeAll
    static void generateKeys() throws JOSEException {
        key = new RSAKeyGenerator(2048).algorithm(JWSAlgorithm.RS256).keyIDFromThumbprint(true).generate();
        otherKey = new RSAKeyGenerator(2048).algorithm(JWSAlgorithm.RS256).keyIDFromThumbprint(true).generate();
    }

    @Test
    @DisplayName("정상 토큰이면 주체와 권한 클레임을 그대로 돌려준다")
    void verifiesValidToken() {
        String token = signer(key, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        JWTClaimsSet claims = verifier(key, policy, clockAt(NOW)).verify(token);

        assertEquals("woojin", claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(List.of("ROLE_USER"), claims.getClaim(AUTHORITIES_CLAIM));
    }

    @Test
    @DisplayName("다른 키 쌍으로 서명된 토큰은 거부한다")
    void rejectsTokenSignedWithAnotherKey() {
        String forged = signer(otherKey, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifier(key, policy, clockAt(NOW)).verify(forged));
    }

    @Test
    @DisplayName("공개키를 HMAC 비밀키로 쓴 HS256 토큰은 알고리즘 단계에서 걸린다")
    void rejectsAlgorithmConfusionAttack() {
        // RSA 공개키는 공개된 값이다. 검증기가 헤더의 alg 를 그대로 믿으면 공격자가
        // 그 공개키를 HMAC 비밀키 삼아 HS256 토큰을 만들어 통과시킬 수 있다.
        byte[] publicKeyBytes = key.toPublicJWK().toJSONString().getBytes(StandardCharsets.UTF_8);
        OctetSequenceKey asSecret = new OctetSequenceKey.Builder(publicKeyBytes).build();
        MacTokenPolicy hs256 = new MacTokenPolicy(JWSAlgorithm.HS256, ISSUER, CLOCK_SKEW);
        String forged = new MacSecuritySigner(asSecret, hs256, TTL, AUTHORITIES_CLAIM, clockAt(NOW))
                .sign("attacker", List.of("ROLE_ADMIN"));

        InvalidBearerTokenException e = assertThrows(InvalidBearerTokenException.class,
                () -> verifier(key, policy, clockAt(NOW)).verify(forged));

        assertTrue(e.getCause() == null, "서명 검증까지 가기 전에 알고리즘에서 걸려야 한다");
    }

    @Test
    @DisplayName("신뢰하지 않는 발급자의 토큰은 거부한다")
    void rejectsTokenFromUntrustedIssuer() {
        RsaTokenPolicy otherIssuer = new RsaTokenPolicy(JWSAlgorithm.RS256, "https://evil.example.com", CLOCK_SKEW);
        String token = signer(key, otherIssuer, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));

        assertThrows(InvalidBearerTokenException.class,
                () -> verifier(key, policy, clockAt(NOW)).verify(token));
    }

    @Test
    @DisplayName("만료 시각에 시계 오차 허용치를 더한 시점을 넘기면 거부한다")
    void rejectsExpiredToken() {
        String token = signer(key, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));
        Instant afterSkew = NOW.plus(TTL).plus(CLOCK_SKEW).plusSeconds(1);

        assertThrows(InvalidBearerTokenException.class,
                () -> verifier(key, policy, clockAt(afterSkew)).verify(token));
    }

    @Test
    @DisplayName("만료 직후라도 시계 오차 허용치 안이면 통과시킨다")
    void acceptsTokenExpiredWithinClockSkew() {
        String token = signer(key, policy, clockAt(NOW)).sign("woojin", List.of("ROLE_USER"));
        Instant withinSkew = NOW.plus(TTL).plusSeconds(1);

        assertEquals("woojin", verifier(key, policy, clockAt(withinSkew)).verify(token).getSubject());
    }

    @Test
    @DisplayName("JWT 형식이 아닌 문자열은 파싱 단계에서 거부한다")
    void rejectsMalformedToken() {
        InvalidBearerTokenException e = assertThrows(InvalidBearerTokenException.class,
                () -> verifier(key, policy, clockAt(NOW)).verify("not-a-jwt"));

        assertInstanceOf(ParseException.class, e.getCause());
    }

    @Test
    @DisplayName("공개키만 있는 JWK 로는 서명자를 만들 수 없다")
    void cannotSignWithPublicKeyOnly() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> signer(key.toPublicJWK(), policy, clockAt(NOW)));

        assertEquals("서명에는 개인키가 필요합니다. 공개키만 있는 JWK 입니다", e.getMessage());
    }

    private static RsaSecuritySigner signer(RSAKey key, RsaTokenPolicy policy, Clock clock) {
        return new RsaSecuritySigner(key, policy, TTL, AUTHORITIES_CLAIM, clock);
    }

    private static RsaTokenVerifier verifier(RSAKey key, RsaTokenPolicy policy, Clock clock) {
        return new RsaTokenVerifier(key, policy, clock);
    }

    private static Clock clockAt(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }
}
