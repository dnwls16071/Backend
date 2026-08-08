package mac.custom;

import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import mac.config.JwtProperties;
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
 * {@code jwt.mode: custom} 일 때 활성화되는 직접 구현 구성.
 *
 * <p>표준 구성과 정확히 같은 정책·키를 쓰되 검증만 {@link MacTokenVerifier} 가 맡는다.
 * 두 방식의 차이를 비교하는 것이 목적이므로 인가 규칙은 양쪽을 동일하게 맞춘다.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "custom")
public class CustomFilterSecurityConfig {

    @Bean
    public MacTokenVerifier macTokenVerifier(OctetSequenceKey key, MacTokenPolicy policy, Clock clock) {
        return new MacTokenVerifier(key, policy, clock);
    }

    @Bean
    public ClaimsAuthenticationConverter claimsAuthenticationConverter(JwtProperties properties) {
        return new ClaimsAuthenticationConverter(properties.authoritiesClaim());
    }

    @Bean
    public SecurityFilterChain customFilterChain(HttpSecurity http,
                                                 MacTokenVerifier verifier,
                                                 ClaimsAuthenticationConverter converter) throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .addFilterBefore(new JwtAuthorizationMacFilter(verifier, converter),
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
