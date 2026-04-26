-- Optional SQL you can run manually in MySQL (or let JPA create tables and only keep index parts).

CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
    product_id BIGINT PRIMARY KEY,
    quantity INT NOT NULL,
    threshold INT NOT NULL,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE IF NOT EXISTS inventory_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    timestamp DATETIME NOT NULL,
    CONSTRAINT fk_tx_product FOREIGN KEY (product_id) REFERENCES product(id)
);

-- Index for low-stock checks. Helps DB quickly navigate by quantity.
CREATE INDEX idx_inventory_quantity ON inventory(quantity);

-- Useful for daily sales aggregation by type and time.
CREATE INDEX idx_tx_type_timestamp ON inventory_transaction(type, timestamp);
