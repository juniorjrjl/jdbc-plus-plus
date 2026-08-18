CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date DATETIMEOFFSET,
    CONSTRAINT fk_payments_orders FOREIGN KEY (order_id) REFERENCES orders(id)
);
