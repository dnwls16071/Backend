package mac;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.http.HttpServletResponse;
import mac.custom.JwtAuthorizationMacFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static mac.MacTokenVerifierTest.clockAt;
import static mac.MacTokenVerifierTest.signerWith;
import static mac.MacTokenVerifierTest.verifierWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthorizationMacFilterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final String ISSUER = "https://auth.example.com";
    private static final OctetSequenceKey KEY = new OctetSequenceKey.Builder("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)).build();

    private final MacTokenPolicy policy = new MacTokenPolicy(JWSAlgorithm.HS256, ISSUER, Duration.ofSeconds(30));
    private final Clock clock = clockAt(NOW);

    private final Converter<JWTClaimsSet, Authentication> converter = claims ->
            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final MockFilterChain chain = new MockFilterChain();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 Bearer 토큰이면 인증을 채우고 다음 필터로 넘긴다")
    void authenticatesValidToken() throws Exception {
        request.addHeader("Authorization", "Bearer " + signValidToken());

        filterWith(converter).doFilter(request, response, chain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "SecurityContext 에 인증이 저장되어야 한다");
        assertEquals("woojin", authentication.getName());
        assertNotNull(chain.getRequest(), "다음 필터로 요청이 전달되어야 한다");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    @DisplayName("토큰이 없으면 인증 없이 그대로 통과시킨다")
    void passesThroughWhenNoToken() throws Exception {
        // 인증이 필요한 자원인지 판단하는 것은 인가(authorization) 단계의 책임이다.
        // 이 필터는 토큰이 있을 때만 관여한다.
        filterWith(converter).doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest(), "다음 필터로 요청이 전달되어야 한다");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    @DisplayName("검증에 실패한 토큰이면 401 로 끊고 다음 필터를 호출하지 않는다")
    void rejectsInvalidToken() throws Exception {
        request.addHeader("Authorization", "Bearer not-a-jwt");

        filterWith(converter).doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(chain.getRequest(), "인증 실패 시 요청이 다음 필터로 넘어가면 안 된다");
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    @DisplayName("서명은 유효해도 인증 객체로 변환할 수 없으면 401 로 끊는다")
    void rejectsWhenConverterReturnsNull() throws Exception {
        request.addHeader("Authorization", "Bearer " + signValidToken());

        filterWith(claims -> null).doFilter(request, response, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(chain.getRequest(), "인증 실패 시 요청이 다음 필터로 넘어가면 안 된다");
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    private JwtAuthorizationMacFilter filterWith(Converter<JWTClaimsSet, Authentication> converter) {
        return new JwtAuthorizationMacFilter(verifierWith(KEY, policy, clock), converter);
    }

    private String signValidToken() {
        return signerWith(KEY, policy, clock).sign("woojin", List.of("ROLE_USER"));
    }
}
