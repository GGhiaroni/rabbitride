package com.gghiaroni.rabbitride.userservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gghiaroni.rabbitride.userservice.user.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private static final String ISSUER = "RabbitRide";

    private final Algorithm algorithm;
    private final long expirationMs;

    public JwtTokenProvider( @Value("${app.jwt.secret}") String secret,
                             @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal){
        Instant agora = Instant.now();
        Instant expira = agora.plusMillis(expirationMs);

        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(principal.getEmail())
            .withClaim("userId", principal.getId().toString())
            .withClaim("name", principal.getNome())
            .withIssuedAt(agora)
            .withExpiresAt(expira)
            .sign(algorithm);
    }

    public String getEmailFromToken(String token) {
        return decode(token).getSubject();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    public boolean isValid(String token) {
        try {
            decode(token);
            return true;
        } catch (JWTVerificationException ex) {
            return false;
        }
    }

    private DecodedJWT decode(String token) {
        return JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build()
            .verify(token);
    }
}
