CREATE TABLE orders(
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    invoice_file BYTEA,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES users(id)
);
