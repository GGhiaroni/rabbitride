package com.gghiaroni.rabbitride.userservice.user;

import com.gghiaroni.rabbitride.userservice.user.dto.LoginRequest;
import com.gghiaroni.rabbitride.userservice.user.dto.LoginResponse;
import com.gghiaroni.rabbitride.userservice.user.dto.RegisterRequest;
import com.gghiaroni.rabbitride.userservice.user.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.email());
        return ResponseEntity.ok(authService.login(request));
    }
}
