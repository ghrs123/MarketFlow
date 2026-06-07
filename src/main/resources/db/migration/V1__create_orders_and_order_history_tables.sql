CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    client_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(16) NOT NULL,
    side VARCHAR(16) NOT NULL,
    quantity NUMERIC(18,8) NOT NULL,
    price NUMERIC(18,8) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders (client_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at);

CREATE TABLE IF NOT EXISTS order_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    previous_status VARCHAR(32),
    new_status VARCHAR(32),
    occurred_at TIMESTAMPTZ NOT NULL,
    payload_json TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_order_history_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
);

CREATE INDEX IF NOT EXISTS idx_order_history_order_id_occurred_at
    ON order_history (order_id, occurred_at);
