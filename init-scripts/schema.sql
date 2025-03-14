CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    description VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (order_id)
);

CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);


CREATE INDEX idx_users_role ON users (role);