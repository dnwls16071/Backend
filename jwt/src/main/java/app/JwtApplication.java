package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * mac 과 rsa 는 형제 패키지라 기본 스캔 범위(선언된 패키지 하위)에 들어오지 않는다.
 * 스캔할 패키지를 명시한다.
 */
@SpringBootApplication(scanBasePackages = {"app", "mac", "rsa"})
@EnableConfigurationProperties(AuthModeProperties.class)
public class JwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtApplication.class, args);
    }
}
