package mac.resourceserver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * {@code jwt.mode: resource-server} 일 때 활성화되는 표준 구성.
 *
 * <p>직접 짠 필터를 등록하는 대신 {@code oauth2ResourceServer} 에 위임하면
 * Spring 이 {@code BearerTokenAuthenticationFilter} 를 대신 꽂아준다.
 */
@Configuration
@ConditionalOnProperty(name = "jwt.mode", havingValue = "resource-server", matchIfMissing = true)
public class ResourceServerSecurityConfig {

    @Bean
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http,
                                                         JwtAuthenticationConverter converter) throws Exception {
        return http
                // JWT 는 요청마다 자격을 들고 오므로 서버 세션이 필요 없다.
                // 세션이 없으면 CSRF 토큰을 둘 곳도 없어 csrf 도 함께 끈다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .build();
    }
}
