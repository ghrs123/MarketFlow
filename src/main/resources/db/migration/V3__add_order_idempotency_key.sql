ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(128);

UPDATE orders
SET idempotency_key = id::text
WHERE idempotency_key IS NULL;

ALTER TABLE orders
    ALTER COLUMN idempotency_key SET NOT NULL;

CREATE UNIQUE INDEX uk_orders_idempotency_key
    ON orders (idempotency_key);
