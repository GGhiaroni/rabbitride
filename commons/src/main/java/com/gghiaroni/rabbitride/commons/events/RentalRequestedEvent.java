package com.gghiaroni.rabbitride.commons.events;

import java.time.Instant;
import java.util.UUID;

public record RentalRequestedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID rentalId,
    UUID userId,
    String userEmail,
    UUID carroId
) {
    public static RentalRequestedEvent of(UUID rentalId, UUID userId, String userEmail, UUID carroId) {
        return new RentalRequestedEvent(UUID.randomUUID(), Instant.now(), rentalId, userId, userEmail, carroId);
    }
}
