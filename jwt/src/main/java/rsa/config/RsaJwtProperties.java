package rsa.config;

import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;
import rsa.RsaTokenPolicy;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * yml 의 {@code jwt.rsa.*} 설정. RSA 방식이 쓰는 값을 전부 여기서 읽는다.
 *
 * <p>발급자·유효기간처럼 대칭키 쪽과 이름이 겹치는 항목도 공유하지 않고 따로 둔다.
 * 키 방식마다 발급자나 만료 정책이 다를 수 있고, 무엇보다 한쪽 설정을 바꿨을 때
 * 다른 쪽이 딸려 움직이면 두 방식을 비교하는 의미가 사라진다.
 *
 * <p>알고리즘을 {@link JWSAlgorithm} 이 아니라 문자열로 받는 이유: 타입으로 받으면
 * {@code @ConfigurationPropertiesBinding} 컨버터가 있어야 하는데, 그 컨버터를 다른
 * 패키지와 공유하면 독립이 깨지고 복제하면 같은 변환이 두 번 등록된다.
 *
 * @param keySize          생성할 키 길이(bit)
 * @param algorithm        허용할 유일한 서명 알고리즘 (RS256/RS384/RS512)
 * @param issuer           발급자
 * @param timeToLive       토큰 유효 기간
 * @param clockSkew        서버 간 시계 오차 허용치
 * @param authoritiesClaim 권한 목록을 담을 클레임 이름
 */
@ConfigurationProperties(prefix = "jwt.rsa")
public record RsaJwtProperties(
        int keySize,
        String algorithm,
        String issuer,
        Duration timeToLive,
        Duration clockSkew,
        String authoritiesClaim
) {

    /** NIST SP 800-57 기준 2048bit 미만 RSA 는 사용 중단 대상이다. Nimbus 도 2048 미만을 거부한다. */
    private static final int MINIMUM_KEY_SIZE = 2048;

    private static final int DEFAULT_KEY_SIZE = 2048;
    private static final String DEFAULT_ALGORITHM = "RS256";
    private static final String DEFAULT_AUTHORITIES_CLAIM = "authorities";

    private static final Set<JWSAlgorithm> SUPPORTED_ALGORITHMS =
            Set.of(JWSAlgorithm.RS256, JWSAlgorithm.RS384, JWSAlgorithm.RS512);

    public RsaJwtProperties {
        keySize = (keySize == 0) ? DEFAULT_KEY_SIZE : keySize;
        algorithm = (algorithm != null) ? algorithm.trim().toUpperCase(Locale.ROOT) : DEFAULT_ALGORITHM;
        clockSkew = (clockSkew != null) ? clockSkew : RsaTokenPolicy.DEFAULT_CLOCK_SKEW;
        authoritiesClaim = (authoritiesClaim != null) ? authoritiesClaim : DEFAULT_AUTHORITIES_CLAIM;

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("jwt.rsa.issuer 은(는) 비어 있을 수 없습니다");
        }
        if (timeToLive == null || timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException("jwt.rsa.time-to-live 는 양수여야 합니다: " + timeToLive);
        }
        if (keySize < MINIMUM_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "jwt.rsa.key-size 는 %d 이상이어야 합니다: %d".formatted(MINIMUM_KEY_SIZE, keySize));
        }
        if (!SUPPORTED_ALGORITHMS.contains(JWSAlgorithm.parse(algorithm))) {
            throw new IllegalArgumentException("RSA 서명에 쓸 수 없는 알고리즘입니다: " + algorithm);
        }
    }

    public JWSAlgorithm jwsAlgorithm() {
        return JWSAlgorithm.parse(algorithm);
    }

    /** 발급·검증이 같은 정책 객체를 공유하도록 여기서 한 번만 만든다. */
    public RsaTokenPolicy toPolicy() {
        return new RsaTokenPolicy(jwsAlgorithm(), issuer, clockSkew);
    }
}
