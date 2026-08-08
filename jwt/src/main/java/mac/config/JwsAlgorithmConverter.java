package mac.config;

import com.nimbusds.jose.JWSAlgorithm;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * yml 의 {@code jwt.algorithm: HS256} 문자열을 {@link JWSAlgorithm} 으로 바꾼다.
 *
 * <p>Spring 은 String 을 받는 생성자를 찾아 자동 변환을 시도하지만, 그 규칙에 기대면
 * Nimbus 쪽 생성자가 바뀌는 순간 조용히 깨진다. 변환을 명시적으로 선언해 둔다.
 */
@Component
@ConfigurationPropertiesBinding
public class JwsAlgorithmConverter implements Converter<String, JWSAlgorithm> {

    @Override
    public JWSAlgorithm convert(String source) {
        return JWSAlgorithm.parse(source.trim().toUpperCase(Locale.ROOT));
    }
}
