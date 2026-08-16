CREATE TABLE payments(
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);
