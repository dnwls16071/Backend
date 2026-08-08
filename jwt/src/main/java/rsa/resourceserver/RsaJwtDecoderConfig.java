package rsa.resourceserver;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import rsa.RsaTokenPolicy;
import rsa.config.RsaJwtProperties;

import java.time.Clock;

/**
 * 직접 구현한 {@code RsaTokenVerifier} 를 Spring Security 표준 구성으로 대체한다.
 *
 * <p>대칭키 쪽과 다른 점은 {@link NimbusJwtDecoder#withPublicKey} 를 쓴다는 것뿐이다.
 * 디코더에 <b>공개키만</b> 넘기므로 이 구성은 토큰을 만들 수 없다.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "rsa-resource-server")
public class RsaJwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(RSAKey key, RsaTokenPolicy policy, Clock clock) {
        // signatureAlgorithm 을 못박는 것이 alg confusion 방어다. 지정하지 않으면 RS256 만
        // 기대하는데, 정책이 RS512 일 때 조용히 어긋난다.
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey(key))
                .signatureAlgorithm(SignatureAlgorithm.from(policy.algorithm().getName()))
                .build();

        decoder.setJwtValidator(validator(policy, clock));
        return decoder;
    }

    /**
     * 기본 validator 는 만료만 본다. 발급자 검증을 직접 얹지 않으면
     * 다른 서비스가 발급한 유효 서명 토큰이 그대로 통과한다.
     */
    private OAuth2TokenValidator<Jwt> validator(RsaTokenPolicy policy, Clock clock) {
        JwtTimestampValidator timestamps = new JwtTimestampValidator(policy.clockSkew());
        timestamps.setClock(clock);

        return new DelegatingOAuth2TokenValidator<>(timestamps, new JwtIssuerValidator(policy.issuer()));
    }

    private java.security.interfaces.RSAPublicKey publicKey(RSAKey key) {
        try {
            return key.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new IllegalStateException("공개키를 추출하지 못했습니다", e);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(RsaJwtProperties properties) {
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
