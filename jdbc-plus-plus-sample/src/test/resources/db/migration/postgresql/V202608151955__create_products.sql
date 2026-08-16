CREATE TABLE products(
    id BIGSERIAL PRIMARY KEY,
    code UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    buy_price DECIMAL(10, 2) NOT NULL,
    stock_amount INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES categories(id)
);
