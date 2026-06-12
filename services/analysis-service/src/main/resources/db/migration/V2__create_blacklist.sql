CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    consumer     VARCHAR(120)             NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE processed_event IS 'Registro de eventos já processados — base da idempotência';
COMMENT
ON COLUMN processed_event.event_id IS 'UUID do evento (mesmo que chegou na mensagem)';
COMMENT
ON COLUMN processed_event.consumer IS 'Identificador do consumer que processou (ex: analysis-service.AnalysisRequestedConsumer)';
COMMENT
ON COLUMN processed_event.processed_at IS 'Timestamp de quando o evento foi processado';

CREATE INDEX idx_processed_event_consumer ON processed_event (consumer);
