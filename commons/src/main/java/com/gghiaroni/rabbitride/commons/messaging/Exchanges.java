package com.gghiaroni.rabbitride.commons.messaging;

public final class Exchanges {

    public static final String RENTAL = "rental.exchange";
    public static final String RENTAL_DLX = "rental.dlx";

    private Exchanges() {
        throw new UnsupportedOperationException("Classe de constantes — não instanciar");
    }
}
