package mac.config;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import mac.signer.MacSecuritySigner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * 대칭키 토큰 <b>발급</b> 설정. mac-custom / mac-resource-server 두 모드가 공유한다.
 *
 * <p>JwtDecoder 방식으로 바꿔도 발급 로직은 그대로다. 달라지는 것은 검증뿐이라
 * 발급을 어느 한쪽 모드에 넣지 않고 여기로 분리했다.
 */
@Configuration
// rsa-* 모드에서는 대칭키 발급기가 필요 없다. 켜두면 Clock 빈이 RSA 구성과 중복되고,
// 쓰지도 않을 jwt.mac.secret 이 유효하기를 요구하게 된다.
@ConditionalOnExpression("'${jwt.mode:mac-resource-server}'.startsWith('mac-')")
@EnableConfigurationProperties(MacJwtProperties.class)
public class MacSignerConfig {

    @Bean
    public MacTokenPolicy macTokenPolicy(MacJwtProperties properties) {
        return properties.toPolicy();
    }

    @Bean
    public OctetSequenceKey macKey(MacJwtProperties properties) {
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
                                               MacJwtProperties properties,
                                               Clock clock) {
        return new MacSecuritySigner(key, policy, properties.timeToLive(), properties.authoritiesClaim(), clock);
    }
}
