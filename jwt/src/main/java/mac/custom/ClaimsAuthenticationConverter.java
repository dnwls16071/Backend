package mac.custom;

import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.ParseException;
import java.util.List;
import java.util.Objects;

/**
 * 검증이 끝난 클레임을 {@link Authentication} 으로 바꾼다.
 * 표준 구성의 {@code JwtAuthenticationConverter} 에 대응하는 자리다.
 *
 * <p>람다가 아니라 클래스로 둔 이유: Spring MVC 는 컨텍스트의 모든 {@link Converter} 빈을
 * FormatterRegistry 에 등록하는데, 람다는 제네릭 타입을 알아낼 수 없어 기동이 실패한다.
 */
public class ClaimsAuthenticationConverter implements Converter<JWTClaimsSet, Authentication> {

    private final String authoritiesClaim;

    public ClaimsAuthenticationConverter(String authoritiesClaim) {
        this.authoritiesClaim = Objects.requireNonNull(authoritiesClaim, "authoritiesClaim");
    }

    @Override
    public Authentication convert(JWTClaimsSet claims) {
        List<String> authorities;
        try {
            authorities = claims.getStringListClaim(this.authoritiesClaim);
        } catch (ParseException e) {
            // 권한 클레임이 문자열 배열이 아니다. 권한 없이 통과시키면 안 되므로
            // null 을 돌려 필터가 401 로 끊게 한다(fail-closed).
            return null;
        }
        if (authorities == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).toList());
    }
}
