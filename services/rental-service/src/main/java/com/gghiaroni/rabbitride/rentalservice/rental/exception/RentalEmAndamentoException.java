package com.gghiaroni.rabbitride.rentalservice.rental.exception;

public class RentalEmAndamentoException extends RuntimeException {

    public RentalEmAndamentoException() {
        super("Você já possui um aluguel em andamento. Conclua-o antes de iniciar outro.");
    }
}
