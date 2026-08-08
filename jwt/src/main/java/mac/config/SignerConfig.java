package mac.config;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import mac.signer.MacSecuritySigner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 토큰 <b>발급</b> 설정. 두 인증 방식(custom / resource-server)이 공유한다.
 *
 * <p>JwtDecoder 방식으로 바꿔도 발급 로직은 그대로다. 달라지는 것은 검증뿐이라 발급을 어느 한쪽 패키지에 넣지 않고 여기로 분리했다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SignerConfig {

    @Bean
    public MacTokenPolicy macTokenPolicy(JwtProperties properties) {
        return properties.toPolicy();
    }

    @Bean
    public OctetSequenceKey macKey(JwtProperties properties) {
        return properties.toKey();
    }

    /**
     * 시계를 빈으로 주입하는 이유: 만료·nbf 판정이 시스템 시각에 직접 묶이면
     * 테스트가 {@code Thread.sleep} 없이는 검증할 수 없게 된다.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public MacSecuritySigner macSecuritySigner(OctetSequenceKey key,
                                              MacTokenPolicy policy,
                                              JwtProperties properties,
                                              Clock clock) {
        return new MacSecuritySigner(key, policy, properties.timeToLive(), properties.authoritiesClaim(), clock);
    }
}
