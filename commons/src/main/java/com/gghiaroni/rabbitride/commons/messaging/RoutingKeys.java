package com.gghiaroni.rabbitride.commons.messaging;

public final class RoutingKeys {

    public static final String RENTAL_REQUESTED = "rental.requested";
    public static final String ANALYSIS_COMPLETED = "rental.analysis.completed";
    public static final String RENTAL_CONFIRMED = "rental.confirmed";
    public static final String RENTAL_FAILED = "rental.failed";

    private RoutingKeys() {
        throw new UnsupportedOperationException("Classe de constantes — não instanciar");
    }
}
