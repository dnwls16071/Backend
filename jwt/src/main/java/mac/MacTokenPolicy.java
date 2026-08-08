package mac;

import com.nimbusds.jose.JWSAlgorithm;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * MAC(대칭키) 기반 JWT 의 발급·검증 정책.
 *
 * <p>알고리즘을 정책으로 못박는 이유: JWT 헤더의 alg 를 그대로 믿으면
 * 공격자가 alg 를 바꿔치기해 검증을 우회할 수 있다. 검증 측이 기대 알고리즘을
 * 먼저 정해두고 헤더가 그것과 일치하는지 확인해야 한다.
 *
 * <p>발급자(issuer)와 알고리즘을 발급·검증이 같은 객체로 공유하면
 * 양쪽 설정이 어긋나 "발급은 되는데 검증은 실패하는" 조합을 원천 차단할 수 있다.
 *
 * @param algorithm 허용할 유일한 서명 알고리즘 (HS256/HS384/HS512)
 * @param issuer    신뢰하는 발급자
 * @param clockSkew 서버 간 시계 오차 허용치. 만료/nbf 판정에만 쓰인다.
 */
public record MacTokenPolicy(JWSAlgorithm algorithm, String issuer, Duration clockSkew) {

    /**
     * MAC 계열 외 알고리즘(none, RS*, ES* 등)을 정책으로 지정하는 실수를 막는다.
     */
    private static final Set<JWSAlgorithm> SUPPORTED_ALGORITHMS = Set.of(JWSAlgorithm.HS256, JWSAlgorithm.HS384, JWSAlgorithm.HS512);

    /**
     * 시계 오차를 따로 지정하지 않았을 때 쓰는 기본 허용치.
     */
    public static final Duration DEFAULT_CLOCK_SKEW = Duration.ofSeconds(30);

    public MacTokenPolicy {
        Objects.requireNonNull(algorithm, "algorithm");
        Objects.requireNonNull(issuer, "issuer");
        Objects.requireNonNull(clockSkew, "clockSkew");

        if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
            throw new IllegalArgumentException("MAC 서명에 쓸 수 없는 알고리즘입니다: " + algorithm);
        }
        if (issuer.isBlank()) {
            throw new IllegalArgumentException("issuer 는 비어 있을 수 없습니다");
        }
        if (clockSkew.isNegative()) {
            throw new IllegalArgumentException("clockSkew 는 음수일 수 없습니다: " + clockSkew);
        }
    }

    public static MacTokenPolicy hs256(String issuer) {
        return new MacTokenPolicy(JWSAlgorithm.HS256, issuer, DEFAULT_CLOCK_SKEW);
    }
}
