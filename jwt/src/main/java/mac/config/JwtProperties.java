package mac.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * yml 의 {@code jwt.*} 설정. 한도·윈도우 같은 값을 코드에 박지 않고 여기로 모은다.
 *
 * <p>검증을 생성자에서 하는 이유: 잘못된 설정은 첫 요청이 들어왔을 때가 아니라 애플리케이션이 뜨는 순간 터져야 한다. 키 길이가 모자란 채로 기동되면 운영 중 모든 토큰 발급이 실패한다.
 *
 * @param mode             어느 방식으로 인증할지. custom(직접 구현) 또는 resource-server(Spring 표준)
 * @param secret           HMAC 대칭키. 알고리즘별 최소 길이를 만족해야 한다
 * @param issuer           발급자. 발급·검증 양쪽이 같은 값을 쓴다
 * @param algorithm        허용할 유일한 서명 알고리즘
 * @param timeToLive       토큰 유효 기간
 * @param clockSkew        서버 간 시계 오차 허용치
 * @param authoritiesClaim 권한 목록을 담을 클레임 이름
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        Mode mode,
        String secret,
        String issuer,
        JWSAlgorithm algorithm,
        Duration timeToLive,
        Duration clockSkew,
        String authoritiesClaim
) {

    public enum Mode {
        /**
         * 직접 구현한 JwtAuthorizationMacFilter 로 인증한다.
         */
        CUSTOM,
        /**
         * Spring Security 의 JwtDecoder + BearerTokenAuthenticationFilter 로 인증한다.
         */
        RESOURCE_SERVER
    }

    /**
     * HMAC 키는 해시 출력 길이 이상이어야 한다(RFC 2104). 미달이면 Nimbus 가 거부한다.
     */
    private static final Map<JWSAlgorithm, Integer> MINIMUM_SECRET_BYTES = Map.of(
            JWSAlgorithm.HS256, 32,
            JWSAlgorithm.HS384, 48,
            JWSAlgorithm.HS512, 64
    );

    private static final Mode DEFAULT_MODE = Mode.RESOURCE_SERVER;
    private static final JWSAlgorithm DEFAULT_ALGORITHM = JWSAlgorithm.HS256;
    private static final String DEFAULT_AUTHORITIES_CLAIM = "authorities";

    public JwtProperties {
        mode = (mode != null) ? mode : DEFAULT_MODE;
        algorithm = (algorithm != null) ? algorithm : DEFAULT_ALGORITHM;
        clockSkew = (clockSkew != null) ? clockSkew : MacTokenPolicy.DEFAULT_CLOCK_SKEW;
        authoritiesClaim = (authoritiesClaim != null) ? authoritiesClaim : DEFAULT_AUTHORITIES_CLAIM;

        requireText(secret, "jwt.secret");
        requireText(issuer, "jwt.issuer");

        if (timeToLive == null || timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException("jwt.time-to-live 는 양수여야 합니다: " + timeToLive);
        }

        Integer minimumBytes = MINIMUM_SECRET_BYTES.get(algorithm);
        if (minimumBytes == null) {
            throw new IllegalArgumentException("MAC 서명에 쓸 수 없는 알고리즘입니다: " + algorithm);
        }
        int actualBytes = secret.getBytes(StandardCharsets.UTF_8).length;
        if (actualBytes < minimumBytes) {
            throw new IllegalArgumentException(
                    "jwt.secret 이 %s 최소 길이에 미달합니다: %d바이트 (최소 %d바이트)"
                            .formatted(algorithm, actualBytes, minimumBytes));
        }
    }

    /**
     * 발급·검증이 같은 정책 객체를 공유하도록 여기서 한 번만 만든다.
     */
    public MacTokenPolicy toPolicy() {
        return new MacTokenPolicy(algorithm, issuer, clockSkew);
    }

    public OctetSequenceKey toKey() {
        return new OctetSequenceKey.Builder(secret.getBytes(StandardCharsets.UTF_8))
                .algorithm(algorithm)
                .build();
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 은(는) 비어 있을 수 없습니다");
        }
    }
}
