package rsa;

import com.nimbusds.jose.JWSAlgorithm;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * RSA(비대칭키) 기반 JWT 의 발급·검증 정책.
 *
 * <p>MacTokenPolicy 와 대칭 구조지만 허용 알고리즘이 다르다. 이 구분이 alg confusion
 * 방어의 핵심이다: RSA 로 검증해야 할 토큰이 HS256 헤더를 달고 오면, 공개키를
 * HMAC 비밀키로 쓰는 고전적인 우회가 성립한다. 정책이 RS* 만 허용하므로 그 조합은
 * 서명 검증 이전에 걸린다.
 *
 * @param algorithm 허용할 유일한 서명 알고리즘 (RS256/RS384/RS512)
 * @param issuer    신뢰하는 발급자
 * @param clockSkew 서버 간 시계 오차 허용치. 만료/nbf 판정에만 쓰인다.
 */
public record RsaTokenPolicy(JWSAlgorithm algorithm, String issuer, Duration clockSkew) {

    private static final Set<JWSAlgorithm> SUPPORTED_ALGORITHMS =
            Set.of(JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512);

    public static final Duration DEFAULT_CLOCK_SKEW = Duration.ofSeconds(30);

    public RsaTokenPolicy {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(issuer, "issuer");
        Objects.requireNonNull(clockSkew, "clockSkew");

        if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
            throw new IllegalArgumentException("RSA 서명에 쓸 수 없는 알고리즘입니다: " + algorithm);
        }
        if (issuer.isBlank()) {
            throw new IllegalArgumentException("issuer 는 비어 있을 수 없습니다");
        }
        if (clockSkew.isNegative()) {
            throw new IllegalArgumentException("clockSkew 는 음수일 수 없습니다: " + clockSkew);
        }
    }

    public static RsaTokenPolicy rs256(String issuer) {
        return new RsaTokenPolicy(JWSAlgorithm.RS256, issuer, DEFAULT_CLOCK_SKEW);
    }
}
