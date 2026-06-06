package com.gghiaroni.rabbitride.commons.messaging;

public final class Queues {

    public static final String ANALYSIS_REQUESTED = "analysis.requested.queue";
    public static final String RENTAL_ANALYSIS_COMPLETED = "rental.analysis.completed.queue";
    public static final String NOTIFICATION = "notification.queue";
    public static final String RENTAL_DLQ = "rental.dlq";

    private Queues() {
        throw new UnsupportedOperationException("Classe de constantes — não instanciar");
    }
}
