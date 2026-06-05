package com.gghiaroni.rabbitride.carservice.car;

public enum StatusCarro {
    DISPONIVEL("Disponível"),
    RESERVADO("Reservado"),
    ALUGADO("Alugado"),
    MANUTENCAO("Manutenção");

    private final String descricao;

    StatusCarro(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
