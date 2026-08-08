package mac.signer;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public abstract class SecuritySigner {

    private final String issuer;
    private final Duration timeToLive;
    private final String authoritiesClaim;
    private final Clock clock;

    protected SecuritySigner(String issuer, Duration timeToLive, String authoritiesClaim, Clock clock) {
        this.issuer = Objects.requireNonNull(issuer, "issuer");
        this.timeToLive = Objects.requireNonNull(timeToLive, "timeToLive");
        this.authoritiesClaim = Objects.requireNonNull(authoritiesClaim, "authoritiesClaim");
        this.clock = Objects.requireNonNull(clock, "clock");
        if (timeToLive.isZero() || timeToLive.isNegative()) {
            throw new IllegalArgumentException("timeToLive 는 양수여야 합니다: " + timeToLive);
        }
    }

    public final String sign(String subject, Collection<String> authorities) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("주체(subject)가 없는 토큰은 발급할 수 없습니다");
        }
        Objects.requireNonNull(authorities, "authorities");

        SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm()), claims(subject, authorities));
        try {
            jwt.sign(jwsSigner());
        } catch (JOSEException e) {
            throw new IllegalStateException("토큰 서명에 실패했습니다", e);
        }
        return jwt.serialize();
    }

    private JWTClaimsSet claims(String subject, Collection<String> authorities) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        return new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .issuer(this.issuer)
                .subject(subject)
                .claim(this.authoritiesClaim, List.copyOf(authorities))
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plus(this.timeToLive)))
                .build();
    }

    protected abstract JWSAlgorithm algorithm();

    protected abstract JWSSigner jwsSigner();
}
