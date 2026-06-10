package com.gghiaroni.rabbitride.rentalservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtTokenValidator {

    private static final String ISSUER = "RabbitRide";

    private final Algorithm algorithm;

    public JwtTokenValidator(@Value("${app.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public AuthenticatedUser validate(String token) {
        DecodedJWT decoded = JWT.require(algorithm)
            .withIssuer(ISSUER)
            .build()
            .verify(token);

        return new AuthenticatedUser(
            UUID.fromString(decoded.getClaim("userId").asString()),
            decoded.getSubject(),
            decoded.getClaim("name").asString()
        );
    }
}
