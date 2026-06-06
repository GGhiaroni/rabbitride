package com.gghiaroni.rabbitride.commons.events;

import java.time.Instant;
import java.util.UUID;

public record RentalConfirmedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID rentalId,
    String userEmail,
    String carroDescricao
) {
    public static RentalConfirmedEvent of(UUID rentalId, String userEmail, String carroDescricao) {
        return new RentalConfirmedEvent(UUID.randomUUID(), Instant.now(), rentalId, userEmail, carroDescricao);
    }
}
