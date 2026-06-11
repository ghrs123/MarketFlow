CREATE TABLE IF NOT EXISTS fix_messages (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    raw_message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fix_messages_order_id UNIQUE (order_id),
    CONSTRAINT fk_fix_messages_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
);
