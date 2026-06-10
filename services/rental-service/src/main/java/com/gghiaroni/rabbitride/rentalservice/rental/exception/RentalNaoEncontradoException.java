package com.gghiaroni.rabbitride.rentalservice.rental.exception;

import java.util.UUID;

public class RentalNaoEncontradoException extends RuntimeException {
    public RentalNaoEncontradoException(UUID id){
        super("Rental não encontrado: " + id);
    }
}
