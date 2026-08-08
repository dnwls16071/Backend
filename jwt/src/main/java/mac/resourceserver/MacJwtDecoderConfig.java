package mac.resourceserver;

import mac.MacTokenPolicy;
import mac.config.MacJwtProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Clock;
import java.util.Map;

/**
 * 직접 구현한 {@code MacTokenVerifier} 를 Spring Security 표준 구성으로 대체한다.
 *
 * <p>대응 관계:
 * <ul>
 *   <li>서명·알고리즘 검증 → {@link NimbusJwtDecoder#withSecretKey}{@code .macAlgorithm(...)}</li>
 *   <li>발급자 검증 → {@link JwtIssuerValidator}</li>
 *   <li>만료·nbf 검증 → {@link JwtTimestampValidator}</li>
 *   <li>클레임 → Authentication 변환 → {@link JwtAuthenticationConverter}</li>
 * </ul>
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "mac-resource-server", matchIfMissing = true)
public class MacJwtDecoderConfig {

    /** JWS 알고리즘 이름 → JCA 키 알고리즘 이름. HS256 은 JCA 에서 HmacSHA256 이다. */
    private static final Map<String, String> JCA_ALGORITHMS = Map.of(
            "HS256", "HmacSHA256",
            "HS384", "HmacSHA384",
            "HS512", "HmacSHA512"
    );

    @Bean
    public JwtDecoder jwtDecoder(MacJwtProperties properties, MacTokenPolicy policy, Clock clock) {
        // macAlgorithm 을 못박는 것이 alg confusion 방어다. 지정하지 않으면 디코더는
        // HS256 만 기대하는데, 정책이 HS512 일 때 조용히 어긋난다.
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey(properties))
                .macAlgorithm(MacAlgorithm.from(policy.algorithm().getName()))
                .build();

        decoder.setJwtValidator(validator(policy, clock));
        return decoder;
    }

    /**
     * 기본 validator 는 만료만 본다. 발급자 검증을 직접 얹지 않으면
     * 다른 서비스가 발급한 유효 서명 토큰이 그대로 통과한다.
     */
    private OAuth2TokenValidator<Jwt> validator(MacTokenPolicy policy, Clock clock) {
        JwtTimestampValidator timestamps = new JwtTimestampValidator(policy.clockSkew());
        timestamps.setClock(clock);

        return new DelegatingOAuth2TokenValidator<>(timestamps, new JwtIssuerValidator(policy.issuer()));
    }

    private SecretKey secretKey(MacJwtProperties properties) {
        String jcaName = JCA_ALGORITHMS.get(properties.algorithm());
        if (jcaName == null) {
            throw new IllegalArgumentException("MAC 서명에 쓸 수 없는 알고리즘입니다: " + properties.algorithm());
        }
        return new SecretKeySpec(properties.toKey().toByteArray(), jcaName);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(MacJwtProperties properties) {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(properties.authoritiesClaim());
        // 기본 접두사는 "SCOPE_" 다. 우리 발급기는 이미 "ROLE_USER" 형태로 넣으므로
        // 그대로 두면 "SCOPE_ROLE_USER" 가 되어 hasRole 검사가 전부 실패한다.
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
