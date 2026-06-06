package com.gghiaroni.rabbitride.commons.events;

import java.time.Instant;
import java.util.UUID;

public record AnalysisCompletedEvent(
    UUID eventId,
    Instant occurredAt,
    UUID rentalId,
    Resultado resultado,
    String motivo
) {
    public enum Resultado {
        APPROVED, REJECTED
    }

    public static AnalysisCompletedEvent approved(UUID rentalId) {
        return new AnalysisCompletedEvent(UUID.randomUUID(), Instant.now(), rentalId, Resultado.APPROVED, null);
    }

    public static AnalysisCompletedEvent rejected(UUID rentalId, String motivo) {
        return new AnalysisCompletedEvent(UUID.randomUUID(), Instant.now(), rentalId, Resultado.REJECTED, motivo);
    }
}
