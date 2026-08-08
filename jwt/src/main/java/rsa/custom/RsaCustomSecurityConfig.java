package rsa.custom;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rsa.RsaTokenPolicy;
import rsa.config.RsaJwtProperties;

import java.time.Clock;

/**
 * {@code jwt.mode: rsa-custom} 일 때 활성화되는 직접 구현 구성.
 *
 * <p>인가 규칙과 진입점은 표준 구성과 동일하게 맞춘다. 두 방식의 차이가 서명·검증에만
 * 있어야 비교가 의미를 갖는다.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "rsa-custom")
public class RsaCustomSecurityConfig {

    @Bean
    public RsaTokenVerifier rsaTokenVerifier(RSAKey key, RsaTokenPolicy policy, Clock clock) {
        return new RsaTokenVerifier(key, policy, clock);
    }

    @Bean
    public ClaimsAuthenticationConverter claimsAuthenticationConverter(RsaJwtProperties properties) {
        return new ClaimsAuthenticationConverter(properties.authoritiesClaim());
    }

    @Bean
    public SecurityFilterChain rsaCustomFilterChain(HttpSecurity http,
                                                    RsaTokenVerifier verifier,
                                                    ClaimsAuthenticationConverter converter) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                // 기본 진입점은 403 을 낸다. 자격 증명이 없어서 거절한 것이므로 401 이 맞다.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .addFilterBefore(new JwtAuthorizationRsaFilter(verifier, converter),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
