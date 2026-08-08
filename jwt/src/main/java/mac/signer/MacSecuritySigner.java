package mac.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import mac.MacTokenPolicy;
import mac.custom.MacTokenVerifier;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * 대칭키(HMAC)로 JWT 를 발급하는 서명자.
 *
 * <p>{@link MacTokenPolicy} 를 {@link MacTokenVerifier} 와 공유해 발급 시 알고리즘·발급자와 검증 시 기대값이 갈라지지 않도록 한다.
 */
public class MacSecuritySigner extends SecuritySigner {

    private final MacTokenPolicy policy;
    private final JWSSigner jwsSigner;

    public MacSecuritySigner(OctetSequenceKey key,
                             MacTokenPolicy policy,
                             Duration timeToLive,
                             String authoritiesClaim,
                             Clock clock) {
        super(Objects.requireNonNull(policy, "policy").issuer(), timeToLive, authoritiesClaim, clock);
        this.policy = policy;
        Objects.requireNonNull(key, "key");
        try {
            this.jwsSigner = new MACSigner(key);
        } catch (JOSEException e) {
            // 키 길이 미달(HS256 은 256bit 이상)처럼 설정 자체가 틀린 경우다. 요청 시점이 아니라 생성 시점에 터뜨려서 잘못된 설정으로 서버가 뜨는 것을 막는다.
            throw new IllegalArgumentException("MAC 서명에 쓸 수 없는 키입니다", e);
        }
    }

    @Override
    protected JWSAlgorithm algorithm() {
        return this.policy.algorithm();
    }

    @Override
    protected JWSSigner jwsSigner() {
        return this.jwsSigner;
    }
}
