package mac.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;

/**
 * yml 의 {@code jwt.mac.*} 설정. RSA 쪽({@code jwt.rsa.*})과 값을 공유하지 않는다.
 * 한쪽 설정을 바꿨을 때 다른 쪽이 딸려 움직이면 두 방식을 비교하는 의미가 사라진다.
 *
 * <p>검증을 생성자에서 하는 이유: 잘못된 설정은 첫 요청이 들어왔을 때가 아니라
 * 애플리케이션이 뜨는 순간 터져야 한다. 키 길이가 모자란 채로 기동되면 운영 중
 * 모든 토큰 발급이 실패한다.
 *
 * @param secret           HMAC 대칭키. 알고리즘별 최소 길이를 만족해야 한다
 * @param algorithm        허용할 유일한 서명 알고리즘 (HS256/HS384/HS512)
 * @param issuer           발급자. 발급·검증 양쪽이 같은 값을 쓴다
 * @param timeToLive       토큰 유효 기간
 * @param clockSkew        서버 간 시계 오차 허용치
 * @param authoritiesClaim 권한 목록을 담을 클레임 이름
 */
@ConfigurationProperties(prefix = "jwt.mac")
public record MacJwtProperties(
        String secret,
        String algorithm,
        String issuer,
        Duration timeToLive,
        Duration clockSkew,
        String authoritiesClaim
) {

    /** HMAC 키는 해시 출력 길이 이상이어야 한다(RFC 2104). 미달이면 Nimbus 가 거부한다. */
    private static final Map<String, Integer> MINIMUM_SECRET_BYTES = Map.of(
            "HS256", 32,
            "HS384", 48,
            "HS512", 64
    );

    private static final String DEFAULT_ALGORITHM = "HS256";
    private static final String DEFAULT_AUTHORITIES_CLAIM = "authorities";

    public MacJwtProperties {
        algorithm = (algorithm != null) ? algorithm.trim().toUpperCase(Locale.ROOT) : DEFAULT_ALGORITHM;
        clockSkew = (clockSkew != null) ? clockSkew : MacTokenPolicy.DEFAULT_CLOCK_SKEW;
        authoritiesClaim = (authoritiesClaim != null) ? authoritiesClaim : DEFAULT_AUTHORITIES_CLAIM;

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.mac.secret 은(는) 비어 있을 수 없습니다");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("jwt.mac.issuer 은(는) 비어 있을 수 없습니다");
        }
        if (timeToLive == null || timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException("jwt.mac.time-to-live 는 양수여야 합니다: " + timeToLive);
        }

        Integer minimumBytes = MINIMUM_SECRET_BYTES.get(algorithm);
        if (minimumBytes == null) {
            throw new IllegalArgumentException("MAC 서명에 쓸 수 없는 알고리즘입니다: " + algorithm);
        }
        int actualBytes = secret.getBytes(StandardCharsets.UTF_8).length;
        if (actualBytes < minimumBytes) {
            throw new IllegalArgumentException(
                    "jwt.mac.secret 이 %s 최소 길이에 미달합니다: %d바이트 (최소 %d바이트)"
                            .formatted(algorithm, actualBytes, minimumBytes));
        }
    }

    public JWSAlgorithm jwsAlgorithm() {
        return JWSAlgorithm.parse(algorithm);
    }

    /** 발급·검증이 같은 정책 객체를 공유하도록 여기서 한 번만 만든다. */
    public MacTokenPolicy toPolicy() {
        return new MacTokenPolicy(jwsAlgorithm(), issuer, clockSkew);
    }

    public OctetSequenceKey toKey() {
        return new OctetSequenceKey.Builder(secret.getBytes(StandardCharsets.UTF_8))
                .algorithm(jwsAlgorithm())
                .build();
    }
}
