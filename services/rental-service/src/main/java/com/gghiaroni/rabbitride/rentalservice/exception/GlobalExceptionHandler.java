package com.gghiaroni.rabbitride.rentalservice.exception;

import com.gghiaroni.rabbitride.rentalservice.rental.exception.RentalEmAndamentoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final URI BASE_ERROR_URI = URI.create("https://rabbitride.com/errors/");

    @ExceptionHandler(RentalEmAndamentoException.class)
    public ProblemDetail handleRentalEmAndamento(RentalEmAndamentoException ex) {
        return build(HttpStatus.CONFLICT, "Aluguel em andamento", ex.getMessage(), "rental-em-andamento");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "Erro de validação",
            "Um ou mais campos estão inválidos.", "validation");
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Erro inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
            "Erro inesperado. Tente novamente em instantes.", "internal");
    }

    private ProblemDetail build(HttpStatus status, String title, String detail, String type) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setType(BASE_ERROR_URI.resolve(type));
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
