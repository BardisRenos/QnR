-- Create orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_id INTEGER UNIQUE,
    description VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for orders
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_order_id ON orders (order_id);

-- Create users table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Index for users
CREATE INDEX idx_users_role ON users (role);

-- Insert initial data into orders
INSERT INTO orders (order_id, description, status) VALUES
(1, 'New order placed', 'PENDING'),
(2, 'Order confirmed', 'PROCESSING'),
(3, 'Shipped to customer', 'COMPLETED'),
(4, 'Customer requested cancellation', 'CANCELLED'),
(5, 'Awaiting payment', 'PENDING'),
(6, 'Processing refund', 'PROCESSING'),
(7, 'Delivered successfully', 'COMPLETED');

-- Insert initial users
INSERT INTO users (username, role, password) VALUES
('John', 'ADMIN', '$2a$10$Y5kU5FkEoB4A9jD7ZnVfXOa9kMkvSBDpSvsIQOVgixlxhj0NO7Y5S'),
('Jane', 'USER', '$2a$10$TdFZpKQHFednGQ4IcI5ckls4Zf8D9RfT38MvT8BD8GpK5p2Ebi3j7K'),
('Emily', 'USER', '$2a$10$h0Vo4hbwDz5SuZnqE.IgrFb3uW9YZlC9DZdYXqxX8P/jNHL92jSFi'),
('Michael', 'USER', '$2a$10$BsI5llWXL1QddzXJlWw6lD6VSbnNCp1f.wgnXY1VQJktUUR0BZyRi'),
('Sarah', 'ADMIN', '$2a$10$JG9f5YabkmfZT.f4mnz/Xz0Iu3fSBPLT2qZqZTqaNr7g91hml09Eq');
