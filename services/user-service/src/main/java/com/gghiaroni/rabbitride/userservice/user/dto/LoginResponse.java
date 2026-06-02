package com.gghiaroni.rabbitride.userservice.user.dto;

import java.util.UUID;

public record LoginResponse(
    String token,
    long expiresIn,
    UUID id
) {
}
