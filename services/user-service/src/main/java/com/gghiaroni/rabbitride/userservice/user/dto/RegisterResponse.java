package com.gghiaroni.rabbitride.userservice.user.dto;

import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String nome,
    String email
) {
}
