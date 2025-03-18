INSERT INTO orders (order_id, description, status) VALUES
(1, 'New order placed', 'PENDING'),
(2, 'Order confirmed', 'PROCESSING'),
(3, 'Shipped to customer', 'COMPLETED'),
(4, 'Customer requested cancellation', 'CANCELLED'),
(5, 'Awaiting payment', 'PENDING'),
(6, 'Processing refund', 'PROCESSING'),
(7, 'Delivered successfully', 'COMPLETED');

INSERT INTO users (username, role, password) VALUES
('John', 'ADMIN', '$2a$10$Y5kU5FkEoB4A9jD7ZnVfXOa9kMkvSBDpSvsIQOVgixlxhj0NO7Y5S'),
('Jane', 'USER', '$2a$10$TdFZpKQHFednGQ4IcI5ckls4Zf8D9RfT38MvT8BD8GpK5p2Ebi3j7K'),
('Emily', 'USER', '$2a$10$h0Vo4hbwDz5SuZnqE.IgrFb3uW9YZlC9DZdYXqxX8P/jNHL92jSFi'),
('Michael', 'USER', '$2a$10$BsI5llWXL1QddzXJlWw6lD6VSbnNCp1f.wgnXY1VQJktUUR0BZyRi'),
('Sarah', 'ADMIN', '$2a$10$JG9f5YabkmfZT.f4mnz/Xz0Iu3fSBPLT2qZqZTqaNr7g91hml09Eq');