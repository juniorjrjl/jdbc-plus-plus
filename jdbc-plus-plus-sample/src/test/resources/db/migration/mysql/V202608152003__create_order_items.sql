CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    amount INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_products FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders(id)
);
