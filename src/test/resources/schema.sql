CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    price DECIMAL(10,2)
);
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total_sum DECIMAL(10,2) NOT NULL
);
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    count INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    order_id BIGINT NOT NULL
);