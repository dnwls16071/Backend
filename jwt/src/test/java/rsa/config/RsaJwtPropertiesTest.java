package rsa.config;

import com.nimbusds.jose.JWSAlgorithm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RsaJwtPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(BindingConfig.class);

    @Test
    @DisplayName("jwt.rsa.* 하이픈 키와 Duration 표기가 그대로 바인딩된다")
    void bindsYmlValues() {
        runner.withPropertyValues(
                        "jwt.rsa.key-size=3072",
                        "jwt.rsa.algorithm=RS512",
                        "jwt.rsa.issuer=https://auth.example.com",
                        "jwt.rsa.time-to-live=10m",
                        "jwt.rsa.clock-skew=30s",
                        "jwt.rsa.authorities-claim=roles")
                .run(context -> {
                    RsaJwtProperties properties = context.getBean(RsaJwtProperties.class);

                    assertThat(properties.keySize()).isEqualTo(3072);
                    assertThat(properties.jwsAlgorithm()).isEqualTo(JWSAlgorithm.RS512);
                    assertThat(properties.issuer()).isEqualTo("https://auth.example.com");
                    assertThat(properties.timeToLive()).isEqualTo(Duration.ofMinutes(10));
                    assertThat(properties.clockSkew()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(properties.authoritiesClaim()).isEqualTo("roles");
                });
    }

    @Test
    @DisplayName("선택 항목을 비우면 기본값이 채워진다")
    void appliesDefaults() {
        runner.withPropertyValues(
                        "jwt.rsa.issuer=https://auth.example.com",
                        "jwt.rsa.time-to-live=10m")
                .run(context -> {
                    RsaJwtProperties properties = context.getBean(RsaJwtProperties.class);

                    assertThat(properties.keySize()).isEqualTo(2048);
                    assertThat(properties.jwsAlgorithm()).isEqualTo(JWSAlgorithm.RS256);
                    assertThat(properties.clockSkew()).isEqualTo(Duration.ofSeconds(30));
                    assertThat(properties.authoritiesClaim()).isEqualTo("authorities");
                });
    }

    @Test
    @DisplayName("2048bit 미만 키는 기동 시점에 거부한다")
    void failsFastOnWeakKeySize() {
        runner.withPropertyValues(
                        "jwt.rsa.key-size=1024",
                        "jwt.rsa.issuer=https://auth.example.com",
                        "jwt.rsa.time-to-live=10m")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("jwt.rsa.key-size 는 2048 이상이어야 합니다"));
    }

    @Test
    @DisplayName("대칭키 알고리즘을 지정하면 기동하지 못한다")
    void failsFastOnMacAlgorithm() {
        // RS* 만 허용하는 것이 alg confusion 방어의 출발점이다.
        runner.withPropertyValues(
                        "jwt.rsa.algorithm=HS256",
                        "jwt.rsa.issuer=https://auth.example.com",
                        "jwt.rsa.time-to-live=10m")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("RSA 서명에 쓸 수 없는 알고리즘입니다: HS256"));
    }

    @Test
    @DisplayName("issuer 가 비어 있으면 기동하지 못한다")
    void failsFastOnBlankIssuer() {
        runner.withPropertyValues("jwt.rsa.time-to-live=10m")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("jwt.rsa.issuer 은(는) 비어 있을 수 없습니다"));
    }

    @Configuration
    @EnableConfigurationProperties(RsaJwtProperties.class)
    static class BindingConfig {
    }
}
