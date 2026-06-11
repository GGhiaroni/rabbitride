CREATE TABLE processed_event
(
    event_id     UUID PRIMARY KEY,
    consumer     VARCHAR(100)             NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_processed_event_processed_at ON processed_event (processed_at);

COMMENT
ON TABLE processed_event IS 'Registro de eventos já processados — garante idempotência em consumers AMQP';
COMMENT
ON COLUMN processed_event.event_id     IS 'UUID do evento extraído do payload (ex: AnalysisCompletedEvent.eventId)';
COMMENT
ON COLUMN processed_event.consumer     IS 'Identificador do consumer que processou — metadata pra debug';
COMMENT
ON COLUMN processed_event.processed_at IS 'Quando processou — útil pra limpeza periódica (retention)';
