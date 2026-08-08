package app;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 어떤 인증 방식을 켤지 고르는 앱 전역 스위치.
 *
 * <p>키 방식(mac/rsa) × 구현 방식(custom/resource-server) 의 네 조합을 고른다.
 * {@code mac}/{@code rsa} 어느 쪽에도 속하지 않는 개념이라 여기에 둔다.
 *
 * <p>열거형이 아니라 문자열로 받는 이유가 핵심이다. 열거형으로 받으면 Spring 의
 * relaxed binding 이 {@code macresourceserver} 같은 표기도 받아준다. 반면
 * {@code @ConditionalOnProperty} 의 {@code havingValue} 는 원시 문자열을 그대로
 * 비교하므로 매칭에 실패한다. 그 틈에 빠지면 어떤 구성도 활성화되지 않은 채로 떠서
 * Spring Boot 기본 보안 체인이 대신 동작한다 — 인증은 걸리는데 우리가 의도한 방식이
 * 아니라 알아채기 가장 어려운 형태의 오설정이다. 그래서 철자를 직접 확인한다.
 *
 * @param mode 인증 방식. 지정하지 않으면 {@code mac-resource-server}
 */
@ConfigurationProperties(prefix = "jwt")
public record AuthModeProperties(String mode) {

    public enum Mode {
        /** 대칭키(HS*) + 직접 구현한 JwtAuthorizationMacFilter. */
        MAC_CUSTOM("mac-custom"),
        /** 대칭키(HS*) + Spring Security 의 JwtDecoder. */
        MAC_RESOURCE_SERVER("mac-resource-server"),
        /** 비대칭키(RS*) + 직접 구현한 JwtAuthorizationRsaFilter. */
        RSA_CUSTOM("rsa-custom"),
        /** 비대칭키(RS*) + Spring Security 의 JwtDecoder. */
        RSA_RESOURCE_SERVER("rsa-resource-server");

        /** yml 과 {@code @ConditionalOnProperty} 가 함께 쓰는 표기. 이 철자만 유효하다. */
        private final String value;

        Mode(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    private static final String DEFAULT_MODE = Mode.MAC_RESOURCE_SERVER.value();

    public AuthModeProperties {
        mode = (mode != null) ? mode.trim() : DEFAULT_MODE;

        if (!isSupported(mode)) {
            throw new IllegalArgumentException(
                    "jwt.mode 값이 올바르지 않습니다: '%s' (가능한 값: %s)".formatted(mode, supportedValues()));
        }
    }

    public Mode toMode() {
        return Arrays.stream(Mode.values())
                .filter(candidate -> candidate.value().equals(mode))
                .findFirst()
                .orElseThrow();
    }

    private static boolean isSupported(String value) {
        return Arrays.stream(Mode.values()).anyMatch(candidate -> candidate.value().equals(value));
    }

    private static String supportedValues() {
        return Arrays.stream(Mode.values()).map(Mode::value).collect(Collectors.joining(", "));
    }
}
