package rsa.custom;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 공개키로 Bearer 토큰을 검증해 SecurityContext 를 채우는 필터.
 *
 * <p>JwtAuthorizationMacFilter 와 흐름이 동일하다. 실제로 다른 것은 주입받는 검증기 타입뿐이며,
 * 두 구현을 나란히 두고 대조하기 위해 별도 클래스로 유지한다.
 */
public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private final RsaTokenVerifier verifier;
    private final Converter<JWTClaimsSet, Authentication> authenticationConverter;

    private BearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();
    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();
    private AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

    public JwtAuthorizationRsaFilter(RsaTokenVerifier verifier,
                                     Converter<JWTClaimsSet, Authentication> authenticationConverter) {
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        this.authenticationConverter = Objects.requireNonNull(authenticationConverter, "authenticationConverter");
    }

    public void setTokenResolver(BearerTokenResolver tokenResolver) {
        this.tokenResolver = Objects.requireNonNull(tokenResolver, "tokenResolver");
    }

    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = Objects.requireNonNull(securityContextRepository, "securityContextRepository");
    }

    public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = Objects.requireNonNull(authenticationEntryPoint, "authenticationEntryPoint");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        final String token;
        try {
            token = this.tokenResolver.resolve(request);
        } catch (AuthenticationException e) {
            reject(request, response, e);
            return;
        }

        // 토큰이 없는 요청은 통과시킨다. 인증이 필요한 자원인지 판단하는 것은 인가 단계의 책임이다.
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            authenticate(request, response, token);
        } catch (AuthenticationException e) {
            reject(request, response, e);
            return;
        }

        chain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, HttpServletResponse response, String token) {
        JWTClaimsSet claims = this.verifier.verify(token);

        Authentication authentication = this.authenticationConverter.convert(claims);
        if (authentication == null) {
            throw new InvalidBearerTokenException("인증 객체로 변환할 수 없는 토큰입니다");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        this.logger.debug("Bearer 토큰 검증 실패", e);
        this.authenticationEntryPoint.commence(request, response, e);
    }
}
