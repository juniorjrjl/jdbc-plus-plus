CREATE TABLE products (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code UNIQUEIDENTIFIER NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(MAX) NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    buy_price DECIMAL(10, 2) NOT NULL,
    stock_amount INT NOT NULL,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_products_categories FOREIGN KEY (category_id) REFERENCES categories(id)
);
