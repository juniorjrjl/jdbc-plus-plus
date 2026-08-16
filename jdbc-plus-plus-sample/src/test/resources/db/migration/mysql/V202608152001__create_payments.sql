CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP NULL,
    CONSTRAINT fk_payments_orders FOREIGN KEY (order_id) REFERENCES orders(id)
);
