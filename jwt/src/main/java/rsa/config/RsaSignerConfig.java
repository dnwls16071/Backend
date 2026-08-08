package rsa.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rsa.RsaTokenPolicy;
import rsa.signer.RsaSecuritySigner;

import java.time.Clock;

/**
 * 비대칭키 토큰 <b>발급</b> 설정. rsa-custom / rsa-resource-server 두 모드가 공유한다.
 */
@Configuration
@ConditionalOnExpression("'${jwt.mode:mac-resource-server}'.startsWith('rsa-')")
@EnableConfigurationProperties(RsaJwtProperties.class)
public class RsaSignerConfig {

    /**
     * 기동할 때마다 키 쌍을 새로 만든다.
     *
     * <p>스터디용 선택이다. 재시작하면 이전에 발급한 토큰은 전부 무효가 되고,
     * 인스턴스를 여러 개 띄우면 서로의 토큰을 검증하지 못한다. 실제 운영에서는
     * 키를 외부(파일/시크릿 매니저/JWK Set)에서 받아와야 한다.
     */
    @Bean
    public RSAKey rsaKey(RsaJwtProperties properties) {
        try {
            return new RSAKeyGenerator(properties.keySize())
                    .algorithm(properties.jwsAlgorithm())
                    .keyIDFromThumbprint(true)
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("RSA 키 쌍을 생성하지 못했습니다", e);
        }
    }

    @Bean
    public RsaTokenPolicy rsaTokenPolicy(RsaJwtProperties properties) {
        return properties.toPolicy();
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
    public RsaSecuritySigner rsaSecuritySigner(RSAKey key,
                                               RsaTokenPolicy policy,
                                               RsaJwtProperties properties,
                                               Clock clock) {
        return new RsaSecuritySigner(key, policy, properties.timeToLive(), properties.authoritiesClaim(), clock);
    }
}
