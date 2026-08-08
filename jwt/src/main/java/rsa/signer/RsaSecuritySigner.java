package rsa.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import rsa.RsaTokenPolicy;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * 개인키(RSA)로 JWT 를 발급하는 서명자.
 *
 * <p>MAC 방식과의 결정적 차이: 서명에는 <b>개인키</b>가, 검증에는 <b>공개키</b>가 쓰인다.
 * 검증 측에 개인키를 넘길 필요가 없으므로 토큰을 검증만 하는 서비스가 토큰을 위조할 수 없다.
 * 대칭키 방식에서는 검증자가 곧 발급자가 될 수 있다.
 */
public class RsaSecuritySigner extends SecuritySigner {

    private final RsaTokenPolicy policy;
    private final JWSSigner jwsSigner;

    public RsaSecuritySigner(RSAKey key,
                             RsaTokenPolicy policy,
                             Duration timeToLive,
                             String authoritiesClaim,
                             Clock clock) {
        super(Objects.requireNonNull(policy, "policy").issuer(), timeToLive, authoritiesClaim, clock);
        this.policy = policy;
        Objects.requireNonNull(key, "key");

        if (!key.isPrivate()) {
            throw new IllegalArgumentException("서명에는 개인키가 필요합니다. 공개키만 있는 JWK 입니다");
        }
        try {
            this.jwsSigner = new RSASSASigner(key);
        } catch (JOSEException e) {
            throw new IllegalArgumentException("RSA 서명에 쓸 수 없는 키입니다", e);
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
