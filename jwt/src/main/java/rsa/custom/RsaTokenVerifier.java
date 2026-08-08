package rsa.custom;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import rsa.RsaTokenPolicy;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/**
 * 공개키로 JWT 를 검증한다. MacTokenVerifier 와 검증 순서·판정 기준을 동일하게 맞춘 대칭 구현.
 *
 * <p>공개키만 보관하므로 이 객체는 토큰을 만들 수 없다.
 */
public class RsaTokenVerifier {

    private final RSASSAVerifier rsaVerifier;
    private final RsaTokenPolicy policy;
    private final Clock clock;

    public RsaTokenVerifier(RSAKey key, RsaTokenPolicy policy, Clock clock) {
        Objects.requireNonNull(key, "key");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
        try {
            // toPublicJWK() 로 공개 성분만 남긴다. 검증기에 개인키를 들려 보낼 이유가 없다.
            this.rsaVerifier = new RSASSAVerifier(key.toPublicJWK());
        } catch (JOSEException e) {
            throw new IllegalArgumentException("RSA 검증에 쓸 수 없는 키입니다", e);
        }
    }

    public JWTClaimsSet verify(String token) {
        SignedJWT jwt = parse(token);
        verifyAlgorithm(jwt);
        verifySignature(jwt);

        JWTClaimsSet claims = extractClaims(jwt);
        verifyIssuer(claims);
        verifyTimeWindow(claims);
        return claims;
    }

    private SignedJWT parse(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new InvalidBearerTokenException("JWT 형식이 아닙니다", e);
        }
    }

    /**
     * 서명 검증보다 먼저 알고리즘을 본다. HS256 헤더를 단 토큰이 여기서 걸리지 않으면
     * 공개키를 HMAC 비밀키로 착각해 검증하는 우회가 성립한다.
     */
    private void verifyAlgorithm(SignedJWT jwt) {
        if (!policy.algorithm().equals(jwt.getHeader().getAlgorithm())) {
            throw new InvalidBearerTokenException(
                    "허용되지 않은 알고리즘입니다: " + jwt.getHeader().getAlgorithm());
        }
    }

    private void verifySignature(SignedJWT jwt) {
        boolean valid;
        try {
            valid = jwt.verify(rsaVerifier);
        } catch (JOSEException e) {
            throw new InvalidBearerTokenException("서명을 검증할 수 없습니다", e);
        }
        if (!valid) {
            throw new InvalidBearerTokenException("서명이 일치하지 않습니다");
        }
    }

    private JWTClaimsSet extractClaims(SignedJWT jwt) {
        try {
            return jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new InvalidBearerTokenException("클레임 형식이 올바르지 않습니다", e);
        }
    }

    private void verifyIssuer(JWTClaimsSet claims) {
        if (!policy.issuer().equals(claims.getIssuer())) {
            throw new InvalidBearerTokenException("신뢰하지 않는 발급자입니다: " + claims.getIssuer());
        }
    }

    private void verifyTimeWindow(JWTClaimsSet claims) {
        Instant now = clock.instant();

        Date expiration = claims.getExpirationTime();
        if (expiration == null) {
            throw new InvalidBearerTokenException("만료 시각(exp)이 없는 토큰입니다");
        }
        if (now.isAfter(expiration.toInstant().plus(policy.clockSkew()))) {
            throw new InvalidBearerTokenException("만료된 토큰입니다");
        }

        Date notBefore = claims.getNotBeforeTime();
        if (notBefore != null && now.isBefore(notBefore.toInstant().minus(policy.clockSkew()))) {
            throw new InvalidBearerTokenException("아직 유효하지 않은 토큰입니다");
        }
    }
}
