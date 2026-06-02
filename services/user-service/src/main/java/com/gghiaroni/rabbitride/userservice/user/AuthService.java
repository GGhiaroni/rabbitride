package com.gghiaroni.rabbitride.userservice.user;

import com.gghiaroni.rabbitride.userservice.security.JwtTokenProvider;
import com.gghiaroni.rabbitride.userservice.user.dto.LoginRequest;
import com.gghiaroni.rabbitride.userservice.user.dto.LoginResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest){
        log.info("AuthService.login called for: {}", loginRequest.email());
        UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
            loginRequest.email(),
            loginRequest.senha()
        );

        Authentication auth = authenticationManager.authenticate(credentials);

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

        String token = jwtTokenProvider.generateToken(userPrincipal);
        long expiresInSeconds = jwtTokenProvider.getExpirationSeconds();

        return new LoginResponse(token, expiresInSeconds, userPrincipal.getId());
    }
}
