package mac.config;

import com.nimbusds.jose.JWSAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class MacJwtPropertiesTest {

    private static final String VALID_SECRET = "0123456789abcdef0123456789abcdef";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(BindingConfig.class);

    @Test
    @DisplayName("yml 의 하이픈 키와 Duration 표기가 그대로 바인딩된다")
    void bindsYmlValues() {
        runner.withPropertyValues(
                        "jwt.mac.secret=" + VALID_SECRET,
                        "jwt.mac.issuer=https://auth.example.com",
                        "jwt.mac.algorithm=HS256",
                        "jwt.mac.time-to-live=10m",
                        "jwt.mac.clock-skew=30s",
                        "jwt.mac.authorities-claim=roles")
                .run(context -> {
                    MacJwtProperties properties = context.getBean(MacJwtProperties.class);

                    assertThat(properties.issuer()).isEqualTo("https://auth.example.com");
                    assertThat(properties.jwsAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
                    assertThat(properties.timeToLive()).isEqualTo(Duration.ofMinutes(10));
                    assertThat(properties.clockSkew()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(properties.authoritiesClaim()).isEqualTo("roles");
                });
    }

    @Test
    @DisplayName("선택 항목을 비우면 기본값이 채워진다")
    void appliesDefaults() {
        runner.withPropertyValues(
                        "jwt.mac.secret=" + VALID_SECRET,
                        "jwt.mac.issuer=https://auth.example.com",
                        "jwt.mac.time-to-live=10m")
                .run(context -> {
                    MacJwtProperties properties = context.getBean(MacJwtProperties.class);

                    assertThat(properties.jwsAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
                    assertThat(properties.clockSkew()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(properties.authoritiesClaim()).isEqualTo("authorities");
                });
    }

    @Test
    @DisplayName("키가 짧으면 첫 요청이 아니라 기동 시점에 실패한다")
    void failsFastOnShortSecret() {
        runner.withPropertyValues(
                        "jwt.mac.secret=too-short",
                        "jwt.mac.issuer=https://auth.example.com",
                        "jwt.mac.time-to-live=10m")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("jwt.mac.secret 이 HS256 최소 길이에 미달합니다"));
    }

    @Test
    @DisplayName("HS512 는 64바이트를 요구하므로 32바이트 키로는 기동하지 못한다")
    void failsFastWhenSecretTooShortForStrongerAlgorithm() {
        runner.withPropertyValues(
                        "jwt.mac.secret=" + VALID_SECRET,
                        "jwt.mac.issuer=https://auth.example.com",
                        "jwt.mac.algorithm=HS512",
                        "jwt.mac.time-to-live=10m")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("최소 64바이트"));
    }

    @Test
    @DisplayName("time-to-live 가 0 이면 기동하지 못한다")
    void failsFastOnNonPositiveTimeToLive() {
        runner.withPropertyValues(
                        "jwt.mac.secret=" + VALID_SECRET,
                        "jwt.mac.issuer=https://auth.example.com",
                        "jwt.mac.time-to-live=0s")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("jwt.mac.time-to-live 는 양수여야 합니다"));
    }

    @Configuration
    @EnableConfigurationProperties(MacJwtProperties.class)
    static class BindingConfig {
    }
}
