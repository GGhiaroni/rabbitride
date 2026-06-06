package com.gghiaroni.rabbitride.commons.events;

import java.time.Instant;
import java.util.UUID;

public record RentalFailedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID rentalId,
    String userEmail,
    String motivo
) {
    public static RentalFailedEvent of(UUID rentalId, String userEmail, String motivo) {
        return new RentalFailedEvent(UUID.randomUUID(), Instant.now(), rentalId, userEmail, motivo);
    }
}
