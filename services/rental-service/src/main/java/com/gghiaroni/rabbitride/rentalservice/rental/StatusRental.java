package com.gghiaroni.rabbitride.rentalservice.rental;

public enum StatusRental {

    PENDENTE("Pendente"),
    EM_ANALISE("Em análise"),
    APROVADO("Aprovado"),
    REJEITADO("Rejeitado"),
    CONFIRMADO("Confirmado"),
    FALHOU("Falhou");

    private final String descricao;

    StatusRental(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
