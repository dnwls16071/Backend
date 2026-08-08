package app;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthModePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(BindingConfig.class);

    @Test
    @DisplayName("정확한 표기는 열거형으로 해석된다")
    void bindsExactValue() {
        runner.withPropertyValues("jwt.mode=rsa-custom")
                .run(context -> assertThat(context.getBean(AuthModeProperties.class).toMode())
                        .isEqualTo(AuthModeProperties.Mode.RSA_CUSTOM));
    }

    @Test
    @DisplayName("지정하지 않으면 mac-resource-server 다")
    void appliesDefault() {
        runner.run(context -> assertThat(context.getBean(AuthModeProperties.class).toMode())
                .isEqualTo(AuthModeProperties.Mode.MAC_RESOURCE_SERVER));
    }

    @Test
    @DisplayName("하이픈을 뺀 표기는 거부한다")
    void rejectsRelaxedSpelling() {
        // 열거형으로 바인딩하면 Spring 의 relaxed binding 이 이 값을 RESOURCE_SERVER 로 받아준다.
        // 하지만 @ConditionalOnProperty 는 원시 문자열을 비교하므로 어떤 구성도 켜지지 않는다.
        // 그 틈을 막는 것이 이 클래스의 존재 이유다.
        runner.withPropertyValues("jwt.mode=macresourceserver")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("jwt.mode 값이 올바르지 않습니다: 'macresourceserver'"));
    }

    @Test
    @DisplayName("대문자 표기도 거부한다")
    void rejectsUpperCase() {
        runner.withPropertyValues("jwt.mode=MAC_RESOURCE_SERVER")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("모르는 값은 가능한 값 목록과 함께 거부한다")
    void rejectsUnknownValue() {
        runner.withPropertyValues("jwt.mode=rsa")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasStackTraceContaining("가능한 값: mac-custom, mac-resource-server, rsa-custom, rsa-resource-server"));
    }

    @Configuration
    @EnableConfigurationProperties(AuthModeProperties.class)
    static class BindingConfig {
    }
}
