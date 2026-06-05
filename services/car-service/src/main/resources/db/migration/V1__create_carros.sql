CREATE TABLE carros
(
    id           UUID PRIMARY KEY,
    placa        VARCHAR(7)               NOT NULL UNIQUE,
    modelo       VARCHAR(50)              NOT NULL,
    marca        VARCHAR(50)              NOT NULL,
    cor          VARCHAR(30)              NOT NULL,
    ano          INTEGER                  NOT NULL,
    valor_diaria NUMERIC(10, 2)           NOT NULL,
    status       VARCHAR(20)              NOT NULL,
    versao       BIGINT                   NOT NULL DEFAULT 0,
    criado_em    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_carros_status ON carros (status);
CREATE INDEX idx_carros_placa ON carros (placa);

COMMENT
ON TABLE carros IS 'Catálogo de veículos disponíveis para aluguel';
COMMENT
ON COLUMN carros.placa        IS 'Placa do veículo (formato Mercosul ou antigo, sem hífen, 7 chars)';
COMMENT
ON COLUMN carros.valor_diaria IS 'Valor da diária em BRL';
COMMENT
ON COLUMN carros.status       IS 'DISPONIVEL, RESERVADO, ALUGADO ou MANUTENCAO';
COMMENT
ON COLUMN carros.versao       IS 'Versão para optimistic locking (@Version) — incrementa a cada UPDATE';
