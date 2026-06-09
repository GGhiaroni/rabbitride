CREATE TABLE rentals
(
    id            UUID PRIMARY KEY,
    user_id       UUID                     NOT NULL,
    user_email    VARCHAR(255)             NOT NULL,
    carro_id      UUID                     NOT NULL,
    status        VARCHAR(20)              NOT NULL,
    motivo_falha  VARCHAR(500),
    criado_em     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rentals_user_id ON rentals (user_id);
CREATE INDEX idx_rentals_status ON rentals (status);
CREATE INDEX idx_rentals_carro_id ON rentals (carro_id);

COMMENT
ON TABLE rentals IS 'Aluguéis de veículos solicitados pelos usuários';
COMMENT
ON COLUMN rentals.user_id       IS 'UUID do usuário no user-service (referência cross-service)';
COMMENT
ON COLUMN rentals.user_email    IS 'Email denormalizado para envio de notificações sem consultar user-service';
COMMENT
ON COLUMN rentals.carro_id      IS 'UUID do carro no car-service (referência cross-service)';
COMMENT
ON COLUMN rentals.status        IS 'PENDENTE → EM_ANALISE → APROVADO/REJEITADO → CONFIRMADO/FALHOU';
COMMENT
ON COLUMN rentals.motivo_falha  IS 'Mensagem descritiva quando status é REJEITADO ou FALHOU';
