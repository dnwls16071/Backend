package mac.custom;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import mac.config.MacJwtProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Clock;

/**
 * {@code jwt.mode: mac-custom} 일 때 활성화되는 직접 구현 구성.
 *
 * <p>표준 구성과 정확히 같은 정책·키를 쓰되 검증만 {@link MacTokenVerifier} 가 맡는다.
 * 두 방식의 차이를 비교하는 것이 목적이므로 인가 규칙은 양쪽을 동일하게 맞춘다.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "mac-custom")
public class MacCustomSecurityConfig {

    @Bean
    public MacTokenVerifier macTokenVerifier(OctetSequenceKey key, MacTokenPolicy policy, Clock clock) {
        return new MacTokenVerifier(key, policy, clock);
    }

    @Bean
    public ClaimsAuthenticationConverter claimsAuthenticationConverter(MacJwtProperties properties) {
        return new ClaimsAuthenticationConverter(properties.authoritiesClaim());
    }

    @Bean
    public SecurityFilterChain macCustomFilterChain(HttpSecurity http,
                                                    MacTokenVerifier verifier,
                                                    ClaimsAuthenticationConverter converter) throws Exception {
        return http
                // JWT 는 요청마다 자격을 들고 오므로 서버 세션이 필요 없다.
                // 세션이 없으면 CSRF 토큰을 둘 곳도 없어 csrf 도 함께 끈다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                // 토큰이 아예 없는 요청은 필터를 그냥 통과해 인가 단계에서 막힌다.
                // 이때 기본 진입점은 Http403ForbiddenEntryPoint 라 403 이 나가는데,
                // 자격 증명이 없어서 거절한 것이므로 RFC 6750 상 401 + WWW-Authenticate 가 맞다.
                // 표준 구성은 oauth2ResourceServer 가 이 진입점을 자동으로 꽂아준다.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .addFilterBefore(new JwtAuthorizationMacFilter(verifier, converter),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
